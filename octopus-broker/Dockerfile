FROM openjdk:17
COPY ./target/octopus-broker*.jar /octopus-broker.jar
EXPOSE 20000
ENTRYPOINT ["java", "-jar", "/octopus-broker.jar"]