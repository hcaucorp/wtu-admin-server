FROM maven:3-jdk-8

RUN mvn clean package

ENTRYPOINT ["java -jar target/server-0.1.0.jar"]

