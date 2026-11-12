FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /build
COPY taller-commons ./taller-commons
COPY ms-clientes-vehiculos ./ms-clientes-vehiculos
RUN mvn -f taller-commons/pom.xml clean install -DskipTests
RUN mvn -f ms-clientes-vehiculos/pom.xml clean package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /build/ms-clientes-vehiculos/target/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
