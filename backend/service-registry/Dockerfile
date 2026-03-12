FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /workspace
COPY parent-pom.xml .
COPY service-registry/pom.xml service-registry/pom.xml
COPY service-registry/src service-registry/src
RUN mvn -f service-registry/pom.xml package -DskipTests --no-transfer-progress

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /workspace/service-registry/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
