FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY target/storage-1.0-SNAPSHOT.jar app.jar
EXPOSE 9093
ENTRYPOINT ["java","-jar","app.jar"]
