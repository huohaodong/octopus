FROM openjdk:17
COPY ./target/octopus-broker*.jar /octopus-broker.jar
EXPOSE 19999 20000
ENTRYPOINT ["java", "-jar", "/octopus-broker.jar"]