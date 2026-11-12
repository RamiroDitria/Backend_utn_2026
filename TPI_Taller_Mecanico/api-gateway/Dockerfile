FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /build
COPY taller-commons ./taller-commons
COPY api-gateway ./api-gateway
RUN mvn -f taller-commons/pom.xml clean install -DskipTests
RUN mvn -f api-gateway/pom.xml clean package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /build/api-gateway/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
