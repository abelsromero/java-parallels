FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app
COPY target/java-parallels-1.0-SNAPSHOT-jar-with-dependencies.jar /app

CMD ["java", "-jar", "/app/java-parallels-1.0-SNAPSHOT-jar-with-dependencies.jar"]
