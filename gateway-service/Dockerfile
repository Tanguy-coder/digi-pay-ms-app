FROM eclipse-temurin:21-alpine
VOLUME /tmp
COPY target/*.jar app.jar
EXPOSE 8888
ENTRYPOINT ["java", "-jar", "app.jar"]
