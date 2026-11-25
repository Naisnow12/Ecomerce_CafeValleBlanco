# Etapa 1: Compilacion con Maven y Java 21
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app

# Copiar archivos del wrapper de Maven y dependencias
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x ./mvnw

# Descargar dependencias
RUN ./mvnw dependency:go-offline -B || true

# Copiar codigo fuente y compilar
COPY src src
RUN ./mvnw clean package -DskipTests -B

# Etapa 2: Imagen ligera de ejecucion con JRE 21
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

ENV PORT=8081
EXPOSE 8081

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-Djava.awt.headless=true", "-jar", "app.jar"]
