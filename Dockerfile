FROM openjdk:17
COPY target/card-service-0.0.1-SNAPSHOT.jar card-service.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/card-service.jar"]
