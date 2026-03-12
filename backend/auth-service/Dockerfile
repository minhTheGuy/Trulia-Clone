FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /workspace
COPY parent-pom.xml .
COPY auth-service/pom.xml auth-service/pom.xml
COPY auth-service/src auth-service/src
RUN mvn -f auth-service/pom.xml package -DskipTests --no-transfer-progress

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /workspace/auth-service/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
