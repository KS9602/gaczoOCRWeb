FROM eclipse-temurin:21-jdk-alpine
RUN apk add --no-cache curl
WORKDIR /app
COPY target/apiGateway-1.0-SNAPSHOT.jar app.jar
EXPOSE 9998
ENTRYPOINT ["java","-jar","app.jar"]
