FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /workspace
COPY parent-pom.xml .
COPY user-service/pom.xml user-service/pom.xml
COPY user-service/src user-service/src
RUN mvn -f user-service/pom.xml package -DskipTests --no-transfer-progress

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /workspace/user-service/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
