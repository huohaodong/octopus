FROM openjdk:11
COPY ./target/*.jar /app.jar
EXPOSE 20000
ENTRYPOINT ["java", "-jar", "/app.jar"]