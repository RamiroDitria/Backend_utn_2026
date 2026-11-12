FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /build
COPY taller-commons ./taller-commons
COPY ms-ordenes ./ms-ordenes
RUN mvn -f taller-commons/pom.xml clean install -DskipTests
RUN mvn -f ms-ordenes/pom.xml clean package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /build/ms-ordenes/target/*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
