# producto-hex-service

API REST de productos construida con **arquitectura hexagonal** (puertos y adaptadores) usando Spring Boot y PostgreSQL.

## Tecnologías

| | Versión |
|---|---|
| Java | 21 |
| Spring Boot | 3.5.7 |
| PostgreSQL | 18 |
| springdoc-openapi | 2.8.9 (Swagger UI) |
| JUnit 5 + Mockito + MockMvc | pruebas |
| H2 | base de datos en memoria para las pruebas |

## Requisitos

- JDK 21
- Maven 3.9+
- PostgreSQL corriendo en `localhost:5432` con la base de datos `topicos_db`

Configuración de conexión en [`application.yml`](src/main/resources/application.yml):

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/topicos_db
    username: postgres
     #esto no se puede hace solo con fin academico el password no debe #mostrarse en Readme
     password: bdatos
```

Si la base de datos no existe:

```sql
CREATE DATABASE topicos_db;
```

La tabla `productos` **no se crea a mano**: si no existe, Hibernate la crea al arrancar a partir de `ProductoEntity` (`ddl-auto: update`).

## Ejecutar

```bash
mvn spring-boot:run
```

La aplicación queda en `http://localhost:8090`.

## Swagger

| | URL |
|---|---|
| Swagger UI | http://localhost:8090/swagger-ui.html |
| OpenAPI (JSON) | http://localhost:8090/v3/api-docs |

Desde Swagger UI se pueden probar todos los endpoints con el botón **Try it out**.

Qué documenta Swagger:

- **Peticiones** con ejemplos reales y la regla de cada campo (`CrearProductoRequest`, `CambiarPrecioRequest`).
- **Cada código de respuesta** (200, 201, 400, 404, 409) con su schema y un ejemplo en el formato [`{ message, timestamp, data, status }`](#formato-de-respuesta). El 400 de `POST` muestra dos ejemplos: *Validación* y *Precio inválido*.
- **Schemas** de respuesta:

| Schema | Uso |
|---|---|
| `RespuestaProducto` | Respuesta con un producto en `data` (crear, obtener, cambiar precio) |
| `RespuestaListaProductos` | Respuesta con una lista de productos en `data` (listar) |
| `RespuestaApi` | Respuesta sin producto: eliminar y todos los errores |

> Las anotaciones de Java no aceptan tipos genéricos (`RespuestaApi<ProductoResponse>.class` no compila). Por eso `RespuestaProducto` y `RespuestaListaProductos` se declaran en [`adapters/in/web/doc/`](src/main/java/com/ejemplo/producto/adapters/in/web/doc/) **solo para la documentación**; en tiempo de ejecución la API devuelve siempre `RespuestaApi<T>`. Los JSON de ejemplo están en [`EjemplosRespuesta`](src/main/java/com/ejemplo/producto/adapters/in/web/doc/EjemplosRespuesta.java).

## Endpoints

Base: `/api/productos`

| Método | Ruta | Descripción | Respuestas |
|---|---|---|---|
| `POST` | `/api/productos` | Crear un producto | 201, 400, 409 |
| `GET` | `/api/productos` | Listar productos | 200 |
| `GET` | `/api/productos/{id}` | Obtener un producto | 200, 404 |
| `PATCH` | `/api/productos/{id}/precio` | Cambiar el precio | 200, 400, 404 |
| `DELETE` | `/api/productos/{id}` | Eliminar un producto | 200, 404 |

### Ejemplos

Crear:

```bash
curl -X POST http://localhost:8090/api/productos \
  -H "Content-Type: application/json" \
  -d '{"codigo":"TEC-0001","nombre":"Teclado","descripcion":"Mecánico","precio":120000,"stock":10,"categoria":"TECNOLOGIA"}'
```

Cambiar precio:

```bash
curl -X PATCH http://localhost:8090/api/productos/1/precio \
  -H "Content-Type: application/json" \
  -d '{"precio":99900}'
```

### Formato de respuesta

**Todas** las respuestas, de éxito y de error, usan el mismo formato ([`RespuestaApi`](src/main/java/com/ejemplo/producto/adapters/in/web/dto/RespuestaApi.java)):

| Campo | Descripción |
|---|---|
| `message` | Mensaje para el cliente |
| `timestamp` | Fecha y hora en que se generó la respuesta |
| `data` | Contenido (`ProductoResponse` o lista de ellos). `null` si no hay nada que devolver; en errores de validación trae el detalle por campo |
| `status` | Código HTTP (igual al de la respuesta) |

Éxito (`GET /api/productos/1`):

```json
{
  "message": "Producto encontrado",
  "timestamp": "2026-11-04T16:45:12.123",
  "data": {
    "id": 1,
    "codigo": "HOG-0001",
    "nombre": "Licuadora Oster",
    "descripcion": null,
    "precio": 250000.00,
    "stock": 25,
    "categoria": "HOGAR",
    "activo": true,
    "fechaCreacion": "2026-11-04T14:54:20.198943"
  },
  "status": 200
}
```

Error de negocio (precio 0):

```json
{
  "message": "El precio debe ser mayor que cero",
  "timestamp": "2026-11-04T16:45:12.123",
  "data": null,
  "status": 400
}
```

Error de validación (detalle por campo en `data`):

```json
{
  "message": "Datos inválidos",
  "timestamp": "2026-11-04T16:45:12.123",
  "data": {
    "codigo": "debe tener el formato AAA-0000",
    "nombre": "no debe estar vacío"
  },
  "status": 400
}
```

| Situación | `status` |
|---|---|
| Producto creado | 201 |
| Consulta, cambio de precio o eliminación correctos | 200 |
| Datos inválidos, precio inválido, JSON mal formado, categoría inexistente, id no numérico | 400 |
| Producto o ruta inexistente | 404 |
| Método HTTP no permitido | 405 |
| Código de producto duplicado | 409 |
| Error inesperado | 500 |

## Arquitectura

Diagrama completo en [`docs/arquitectura.drawio`](docs/arquitectura.drawio). Se abre en [app.diagrams.net](https://app.diagrams.net) (*File → Open from → Device*) o en VS Code con la extensión **Draw.io Integration** (`hediet.vscode-drawio`).

```
          ┌──────────────── adapters/in/web ────────────────┐
HTTP ───► │ ProductoController ──► PortIn (interfaces)       │
     ◄─── │ RespuestaApi { message, timestamp, data, status }│
          └──────────────────────────┬──────────────────────┘
                                     │
          ┌────────────────────── core ─────────────────────┐
          │ usecase/  CrearProducto, ObtenerProducto,        │
          │           CambiarPrecioProducto, EliminarProducto│
          │ model/    Producto  ← reglas de negocio          │
          │ ports/out Buscar/Guardar/EliminarProductoPortOut │
          └──────────────────────────┬──────────────────────┘
                                     │
          ┌────────────── adapters/out/persistence ─────────┐
          │ ProductoRepositoryAdapter ──► Spring Data JPA    │ ──► PostgreSQL
          └─────────────────────────────────────────────────┘
```

- **`core`**: el núcleo. No depende de Spring, JPA ni HTTP.
  - `model/Producto`: datos y **reglas de negocio** (precio > 0, máximo 2 decimales, código `AAA-0000`, stock ≥ 0).
  - `usecase/`: coordina cada acción (buscar → aplicar regla → guardar).
  - `ports/in/*PortIn`: lo que el exterior puede pedir al núcleo. `CrearProductoCommand` agrupa los datos para crear.
  - `ports/out/*PortOut`: lo que el núcleo necesita del exterior, separado en `BuscarProductoPortOut`, `GuardarProductoPortOut` y `EliminarProductoPortOut`. Cada caso de uso depende solo de los que usa.
- **`adapters/in/web`**: traduce HTTP ↔ dominio. Controlador, DTOs y manejo de errores.
  - `dto/RespuestaApi<T>`: envoltorio de todas las respuestas `{ message, timestamp, data, status }`. `ProductoResponse` va dentro de `data`.
  - `GlobalExceptionHandler`: convierte cada excepción (del dominio, de validación, de HTTP o de la base de datos) en un `RespuestaApi` con su código.
  - `doc/`: ejemplos y schemas para Swagger. No afectan el comportamiento.
- **`adapters/out/persistence`**: `ProductoRepositoryAdapter` implementa los tres `PortOut` con JPA.

### Qué puerto de salida usa cada caso de uso

| Caso de uso | Buscar | Guardar | Eliminar |
|---|:---:|:---:|:---:|
| `CrearProducto` | | ✔ | |
| `ObtenerProducto` | ✔ | | |
| `CambiarPrecioProducto` | ✔ | ✔ | |
| `EliminarProducto` | ✔ | | ✔ |
- **`config/BeanConfig`**: registra los casos de uso como beans, así el núcleo no necesita `@Service`.

Las dependencias siempre apuntan **hacia el núcleo**: los adaptadores conocen a `core`, pero `core` no conoce a los adaptadores.

## Lógica de negocio

La lógica de negocio vive en `core` y se reparte según una pregunta: **¿la regla necesita algo de fuera?**

| Tipo de regla | Dónde va | Ejemplo |
|---|---|---|
| Solo necesita los datos del producto | **Modelo** (`Producto`) | El precio debe ser mayor que 0 |
| Necesita consultar o guardar algo | **Caso de uso** (usando un `PortOut`) | No se puede eliminar un producto que no existe |

Reglas en [`Producto`](src/main/java/com/ejemplo/producto/core/model/Producto.java):

- Código con formato `AAA-0000`.
- Nombre obligatorio, máximo 100 caracteres.
- Descripción de máximo 255 caracteres.
- Precio obligatorio, mayor que 0 y con máximo 2 decimales (`PrecioInvalidoException`).
- Stock no negativo.
- Un producto nuevo empieza activo y con la fecha actual.

Todas se validan en el constructor privado, así que **no puede existir un `Producto` inválido**. Si `Producto` fuera solo getters y setters (modelo anémico), cualquiera podría hacer `setPrecio(-5)`.

Los casos de uso no validan datos: coordinan los pasos y delegan las reglas al modelo.

```java
// CambiarPrecioProducto
Producto producto = buscarProducto.buscarPorId(id)               // 1. buscar
        .orElseThrow(() -> new ProductoNoEncontradoException(id)); //    si no existe → error
producto.cambiarPrecio(nuevoPrecio);                             // 2. el modelo aplica la regla
return guardarProducto.guardar(producto);                        // 3. guardar
```

La lógica de negocio **no** está en el controlador, en los DTOs ni en la persistencia. Las validaciones de los DTOs (`@NotNull`, `@Pattern`) solo revisan el formato de la petición, y los `CHECK` de la tabla son una protección adicional en la base de datos.

## ¿Por qué `BeanConfig` y no `@Service`?

Si cada caso de uso llevara `@Service`, Spring los detectaría solo y `BeanConfig` sobraría. Pero entonces `core` importaría Spring y dejaría de ser independiente del framework.

| | `BeanConfig` | `@Service` en el caso de uso |
|---|---|---|
| ¿`core` depende de Spring? | No | Sí |
| ¿Cuánto código? | Un archivo extra | Menos |
| ¿Es hexagonal "pura"? | Sí | Es un compromiso |

Con `BeanConfig` los casos de uso son clases Java normales y se prueban con `new CambiarPrecioProducto(...)`, sin levantar Spring. Las anotaciones de Spring solo aparecen en los adaptadores y en `config`.

## Principios SOLID

| Principio | Cómo se aplica |
|---|---|
| **S**: Responsabilidad única | Cada clase tiene un solo motivo para cambiar: `Producto` (reglas), un caso de uso por acción, `ProductoController` (HTTP), `GlobalExceptionHandler` (errores → HTTP), `ProductoMapper` (dominio ↔ entidad), `ProductoRepositoryAdapter` (base de datos). |
| **O**: Abierto/cerrado | Una acción nueva es un `PortIn` y un caso de uso nuevos, sin tocar los existentes. Otra forma de guardar (MongoDB, memoria) es otro adaptador que implementa los `PortOut`, sin tocar el núcleo. `CrearProductoCommand` permite agregar campos sin cambiar la firma de `CrearProductoPortIn`. |
| **L**: Sustitución de Liskov | Cualquier implementación de un puerto reemplaza a otra. Las pruebas lo demuestran: los casos de uso funcionan con mocks del repositorio y el controlador con mocks de los casos de uso. |
| **I**: Segregación de interfaces | Cuatro `PortIn` pequeños en vez de un `ProductoService` grande. Tres `PortOut` (`Buscar`, `Guardar`, `Eliminar`) en vez de un repositorio con todo: cada caso de uso depende solo de lo que usa. |
| **D**: Inversión de dependencias | El núcleo define las interfaces (`PortOut`) y los adaptadores las implementan. Nadie crea dependencias dentro del núcleo: `BeanConfig` las inyecta desde fuera. |

```
ProductoController ──► CrearProductoPortIn ◄── CrearProducto ──► GuardarProductoPortOut ◄── ProductoRepositoryAdapter
     (adapter)            (abstracción)          (core)            (abstracción)               (adapter)
```

## `CrearProductoCommand`: ¿es el patrón Command?

No exactamente. Se llama igual, pero son dos cosas distintas:

| | Command de GoF | `CrearProductoCommand` |
|---|---|---|
| ¿Qué es? | Un objeto que **ejecuta** una acción | Un objeto que **lleva datos** |
| ¿Tiene `execute()`? | Sí | No, es un `record` solo con datos |
| ¿Para qué sirve? | Encolar, deshacer (*undo*) o registrar acciones | Agrupar los parámetros de entrada de un caso de uso |

El Command de GoF se vería así:

```java
interface Comando { void ejecutar(); }
class CrearProductoComando implements Comando {
    public void ejecutar() { /* crea el producto */ }
}
```

`CrearProductoCommand` es un **Command Object** al estilo de CQRS y la arquitectura hexagonal: un mensaje que expresa una intención ("quiero crear este producto"). Lo ejecuta el caso de uso `CrearProducto`, no el comando. También se le llama **Parameter Object**.

Flujo:

```
CrearProductoRequest ──aComando()──► CrearProductoCommand ──► CrearProducto ──► Producto.crear(...)
     (adapters/in/web)                  (core/ports/in)         (core/usecase)     (core/model)
```

Convención (Tom Hombergs, *Get Your Hands Dirty on Clean Architecture*): `Command` para las acciones que modifican y `Query` para las consultas.

## Base de datos

Tabla `productos`:

| Columna | Tipo | Restricción |
|---|---|---|
| `id` | `BIGINT` identity | PK |
| `codigo` | `VARCHAR(8)` | único, formato `AAA-0000` |
| `nombre` | `VARCHAR(100)` | obligatorio |
| `descripcion` | `VARCHAR(255)` | opcional |
| `precio` | `NUMERIC(12,2)` | `> 0` |
| `stock` | `INTEGER` | `>= 0` |
| `categoria` | `VARCHAR(20)` | `TECNOLOGIA`, `HOGAR`, `ROPA`, `ALIMENTOS`, `OTROS` |
| `activo` | `BOOLEAN` | obligatorio |
| `fecha_creacion` | `TIMESTAMP(6)` | obligatorio |

Índices: `uk_productos_codigo` (único), `idx_productos_nombre`, `idx_productos_categoria`, `idx_productos_categoria_activo`.

> Índices y restricciones `CHECK` están declarados en `ProductoEntity` (`@Table`, `@Check`). Si la tabla ya existe, Hibernate no la recrea.

## Pruebas

```bash
mvn test
```

| Prueba | Qué verifica | Necesita |
|---|---|---|
| `ProductoTest` | Reglas de negocio del modelo | Nada (Java puro) |
| `CambiarPrecioProductoTest` | Caso de uso con repositorio simulado (Mockito) | Nada |
| `ProductoControllerTest` | Endpoints y códigos HTTP (MockMvc) | Nada |
| `ProductoRepositoryAdapterTest` | Acceso a la base de datos (JPA + esquema creado por Hibernate) | Nada (H2 en memoria) |

La prueba de persistencia usa el perfil `test` (`src/test/resources/application-test.yml`): una base H2 en memoria en modo PostgreSQL, donde Hibernate crea la tabla. No hace falta tener PostgreSQL instalado para correr `mvn test`.
