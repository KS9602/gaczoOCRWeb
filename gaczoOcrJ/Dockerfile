FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY target/gaczoOcrJ-1.0-SNAPSHOT.jar app.jar
EXPOSE 9090
ENTRYPOINT ["java","-jar","app.jar"]
