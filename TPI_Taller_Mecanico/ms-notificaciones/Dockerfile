FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /build
COPY taller-commons ./taller-commons
COPY ms-notificaciones ./ms-notificaciones
RUN mvn -f taller-commons/pom.xml clean install -DskipTests
RUN mvn -f ms-notificaciones/pom.xml clean package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /build/ms-notificaciones/target/*.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]
