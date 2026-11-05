# CI/CD: de un push a producción en Render

Este documento explica paso a paso cómo se prueba, empaqueta y despliega `producto-hex-service`.

- **Workflow:** [`.github/workflows/ci.yml`](../.github/workflows/ci.yml)
- **Imagen Docker:** [`Dockerfile`](../Dockerfile)
- **Aplicación publicada:** <https://producto-hex-service.onrender.com/swagger-ui.html>

## 1. Resumen

```text
git push a main
      │
      ▼
GitHub Actions
  ├─ Job 1: Compilar y probar ──── si falla, se detiene aquí y no se despliega
  │
  └─ Job 2: Imagen Docker y despliegue
        ├─ construye la imagen
        ├─ la sube a ghcr.io
        └─ avisa a Render (deploy hook)
                  │
                  ▼
            Render descarga la imagen :latest,
            arranca la app y cambia el tráfico a la versión nueva
```

| Evento | Job 1: pruebas | Job 2: imagen y despliegue |
|---|---|---|
| Push a `main` | Sí | Sí, si las pruebas pasan |
| Pull request hacia `main` | Sí | No |
| Push a otra rama | No | No |

**CI** (integración continua) es el job 1: comprobar automáticamente que el código compila y pasa las pruebas.
**CD** (despliegue continuo) es el job 2: publicar esa versión sin pasos manuales.

## 2. Configuración inicial

Solo se hace una vez.

### Paso 1: subir el código a GitHub

GitHub Actions lee el workflow de la carpeta `.github/workflows/`. Con subir el archivo a `main` ya queda activo:

```bash
git push origin main
```

La rama debe llamarse `main`, porque es la que indica el workflow en `on.push.branches`.

### Paso 2: hacer pública la imagen

El primer workflow crea el paquete en GitHub Container Registry, pero este suele quedar privado. Para que Render lo descargue sin credenciales:

1. En GitHub, abra su perfil y entre en **Packages** → `producto-hex-service`.
2. Entre en **Package settings** → **Change visibility** → **Public**.

### Paso 3: crear el servicio web en Render

1. Entre en **New** → **Web Service** → **Existing Image**.
2. Escriba la imagen: `ghcr.io/ccaceresumtuser2/producto-hex-service:latest`.
3. Elija la misma región que la base de datos. El hostname interno de la base solo funciona desde esa región.

### Paso 4: configurar las variables de entorno en Render

En la pestaña **Environment** del servicio:

| Variable | Valor |
|---|---|
| `DB_URL` | `jdbc:postgresql://<hostname-interno>:5432/<base-de-datos>` |
| `DB_USERNAME` | Usuario de la base de datos |
| `DB_PASSWORD` | Contraseña de la base de datos |

`DB_URL` debe empezar con `jdbc:`. La URL `postgresql://...` que muestra Render no sirve tal cual para Java.

`PORT` no se configura: la imagen trae 8080 por defecto y Render puede asignar el suyo.

> Las credenciales nunca van en el código ni en este documento. `application.yml` solo contiene valores para desarrollo local.

### Paso 5: conectar GitHub con Render

1. En Render, abra el servicio web, entre en **Settings** → **Deploy Hook** y copie la URL.
2. En GitHub, abra el repositorio y entre en **Settings** → **Secrets and variables** → **Actions**.
3. En la pestaña **Secrets**, sección **Repository secrets**, pulse **New repository secret**.
4. Escriba el nombre `RENDER_DEPLOY_HOOK_URL` y pegue la URL.

Debe ser un **secreto**, no una variable, porque cualquiera que tenga esa URL puede lanzar despliegues.

### Paso 6: desactivar el Auto-Deploy de Render

En Render, entre en **Settings** y ponga **Auto-Deploy** en **No**. Así solo despliega GitHub Actions, después de que pasen las pruebas.

## 3. Qué hace el workflow

### Job 1: Compilar y probar

Se ejecuta en una máquina Ubuntu 24.04 nueva, que se descarta al terminar.

| # | Paso | Qué hace |
|---|---|---|
| 1 | Descargar el código | Clona el repositorio en la máquina. |
| 2 | Instalar Java 21 | Instala Temurin 21 y guarda en caché las dependencias de Maven. |
| 3 | Dar permiso al Maven Wrapper | Ejecuta `chmod +x mvnw`, porque el permiso se pierde si el repositorio se creó desde Windows. |
| 4 | Compilar y ejecutar pruebas | Ejecuta `./mvnw -B verify`. Si una prueba falla, el workflow se detiene. |
| 5 | Publicar el jar | Guarda el jar como descarga del workflow durante 7 días. |
| 6 | Publicar reportes de pruebas | Solo si algo falla: guarda `target/surefire-reports/` para ver el motivo. |

Las pruebas no necesitan PostgreSQL. El perfil `test` ([`application-test.yml`](../src/test/resources/application-test.yml)) usa una base H2 en memoria.

### Job 2: Imagen Docker y despliegue en Render

Tiene dos condiciones:

```yaml
needs: build                      # solo si el job 1 terminó bien
if: github.event_name == 'push' && github.ref == 'refs/heads/main'   # solo en push a main
```

| # | Paso | Qué hace |
|---|---|---|
| 1 | Descargar el código | Clona el repositorio. Cada job empieza en una máquina distinta. |
| 2 | Nombre de la imagen | Arma `ghcr.io/ccaceresumtuser2/producto-hex-service`. ghcr.io exige minúsculas. |
| 3 | Iniciar sesión en ghcr.io | Usa `GITHUB_TOKEN`, que GitHub crea solo en cada ejecución. |
| 4 | Preparar Docker Buildx | Prepara el constructor de imágenes con soporte de caché. |
| 5 | Construir y subir la imagen | Ejecuta el `Dockerfile` y sube la imagen con dos etiquetas. |
| 6 | Desplegar en Render | Hace un `POST` a la URL del deploy hook y muestra la respuesta. |

Las dos etiquetas de la imagen sirven para cosas distintas:

- `:latest` es la que usa Render.
- `:<hash-del-commit>` identifica exactamente qué código contiene cada imagen.

El paso 6 tiene tres resultados posibles:

| Situación | Resultado |
|---|---|
| Falta el secreto | Aviso amarillo; el workflow sigue en verde, pero **no despliega**. |
| Render acepta (HTTP menor que 300) | Paso en verde y Render empieza a desplegar. |
| Render rechaza (HTTP 300 o mayor) | Paso en rojo, con la respuesta de Render en el log. |

### El Dockerfile

Tiene dos etapas para que la imagen final sea pequeña:

1. **Compilar:** usa el JDK 21 y genera el jar con `-DskipTests`, porque las pruebas ya pasaron en el job 1.
2. **Ejecutar:** usa solo el JRE 21 y copia el jar. Maven y el código fuente no quedan en la imagen. La app corre con un usuario sin privilegios (`app`).

### Qué ocurre en Render

1. Recibe el aviso del deploy hook.
2. Descarga la imagen `:latest`.
3. Arranca la app y espera a que abra el puerto.
4. Cambia el tráfico a la versión nueva. **Si falla, la versión anterior sigue activa.**

En el primer arranque, Hibernate crea la tabla `productos` (`ddl-auto: update`).

## 4. El día a día

1. Haga sus cambios y pruebe en local:

   ```bash
   ./mvnw test
   ```

2. Suba los cambios:

   ```bash
   git add <archivos>
   git commit -m "Descripción del cambio"
   git push origin main
   ```

3. Siga el workflow en la pestaña **Actions** del repositorio.
4. Cuando termine, espere unos **2 a 5 minutos**. Mientras Render prepara la versión nueva, sigue respondiendo la anterior.
5. En Render, pestaña **Events**, compruebe que aparezca **Deploy live**.
6. Compruebe la app:

   ```bash
   curl https://producto-hex-service.onrender.com/api/productos
   ```

En el plan gratuito, Render apaga el servicio tras unos 15 minutos sin uso. La primera petición lo despierta y tarda entre 30 y 60 segundos; mientras tanto puede mostrar la pantalla *Service waking up* o un error 502.

La raíz `/` devuelve 404 porque la app no define esa ruta. Use `/api/productos` o `/swagger-ui.html`.

## 5. Problemas frecuentes

| Síntoma | Causa | Solución |
|---|---|---|
| `package ...ports.out does not exist` en CI, pero en local compila | Una regla `out/` en `.gitignore` también ignora los paquetes `out` del código | Usar `/out/`, que solo ignora la carpeta de la raíz, y subir los paquetes |
| El workflow sale en verde, pero Render no despliega | Falta el secreto `RENDER_DEPLOY_HOOK_URL` | Crearlo (paso 5). Busque el aviso en el paso **Desplegar en Render** |
| `curl: (22) ... error: 400` o HTTP 400 al desplegar | Render rechazó la petición; ocurrió al enviar `imgURL` en la URL del hook | Llamar al hook sin `imgURL` y revisar que el secreto sea el del servicio web |
| `No open ports detected` en Render | La app todavía está arrancando | Esperar. Si termina en `Exited with status 1`, buscar el error en las líneas anteriores |
| `Exited with status 1` después de conectar a la base | La app falló al arrancar | Buscar `APPLICATION FAILED TO START` o `Caused by:` en el log de Render |
| El cambio no aparece después del workflow | El despliegue de Render todavía no termina | Esperar **Deploy live** en **Events**. Si ya terminó, recargar con **Ctrl + F5** |
| Render no puede descargar la imagen | El paquete de ghcr.io es privado | Hacerlo público (paso 2) |
| No conecta a la base de datos | `DB_URL` sin `jdbc:`, credenciales incorrectas o región distinta | Revisar las variables (paso 4) y la región |
| Avisos `Node.js 20 is deprecated` | Acciones desactualizadas | Subir de versión las acciones en `ci.yml`. Es un aviso, no un error |

## 6. Dónde mirar cuando algo falla

| Qué quiere saber | Dónde verlo |
|---|---|
| ¿Pasaron las pruebas? | GitHub → **Actions** → job **Compilar y probar** |
| ¿Se subió la imagen? | GitHub → perfil → **Packages** |
| ¿Render aceptó el despliegue? | GitHub → **Actions** → paso **Desplegar en Render** |
| ¿Terminó el despliegue? | Render → **Events** |
| ¿Por qué no arrancó la app? | Render → **Logs** |
