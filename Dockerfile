FROM openjdk:17
ARG JAR_FILE=target/*.jar
COPY ./target/TelegramBot-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 5432
ENTRYPOINT ["java","-jar","/app.jar"]

