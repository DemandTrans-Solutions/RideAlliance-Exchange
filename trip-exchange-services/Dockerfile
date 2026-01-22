FROM --platform=linux/amd64 eclipse-temurin:21-jre
WORKDIR /app
COPY target/clearinghouse.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
