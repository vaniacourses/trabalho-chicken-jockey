FROM openjdk:8-jdk-alpine

WORKDIR /app

COPY target/pdv-0.0.1-SNAPSHOT.war app.war

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.war"]