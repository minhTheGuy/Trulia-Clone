FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /workspace
COPY parent-pom.xml .
COPY file-service/pom.xml file-service/pom.xml
COPY file-service/src file-service/src
RUN mvn -f file-service/pom.xml package -DskipTests --no-transfer-progress

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /workspace/file-service/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
