# ---------- Etapa 1: compilar el jar ----------
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Primero solo el wrapper y el pom: así Docker reutiliza la capa de dependencias
# mientras el pom no cambie y las siguientes compilaciones son más rápidas.
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw -B dependency:go-offline

COPY src src
# Las pruebas ya se ejecutan en GitHub Actions; aquí solo se empaqueta.
RUN ./mvnw -B package -DskipTests

# ---------- Etapa 2: imagen final, solo con el JRE y el jar ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

# No ejecutar la aplicación como root.
RUN useradd --system --create-home app && mkdir -p /app/logs && chown app /app/logs
USER app

COPY --from=build /app/target/*.jar app.jar

# Render asigna el puerto en la variable PORT (application.yml la lee).
ENV PORT=8090
EXPOSE 8090

# MaxRAMPercentage: la JVM usa hasta el 75 % de la memoria del contenedor (planes pequeños de Render).
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
