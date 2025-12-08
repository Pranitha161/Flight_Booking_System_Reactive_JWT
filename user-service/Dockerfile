
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

COPY target/user-service-auth-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8083

ENTRYPOINT ["java","-jar","app.jar"]
