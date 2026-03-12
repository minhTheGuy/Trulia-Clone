FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /workspace
COPY parent-pom.xml .
COPY transaction-service/pom.xml transaction-service/pom.xml
COPY transaction-service/src transaction-service/src
RUN mvn -f transaction-service/pom.xml package -DskipTests --no-transfer-progress

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /workspace/transaction-service/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
