FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /workspace
COPY parent-pom.xml .
COPY api-gateway/pom.xml api-gateway/pom.xml
COPY api-gateway/src api-gateway/src
RUN mvn -f api-gateway/pom.xml package -DskipTests --no-transfer-progress

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /workspace/api-gateway/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
