FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /workspace
COPY parent-pom.xml .
COPY property-service/pom.xml property-service/pom.xml
COPY property-service/src property-service/src
RUN mvn -f property-service/pom.xml package -DskipTests --no-transfer-progress

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /workspace/property-service/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
