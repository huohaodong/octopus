FROM openjdk:11
COPY ./target/octopus.jar /octopus.jar
EXPOSE 20000
ENTRYPOINT ["java", "-jar", "/octopus.jar"]