# build: docker build -t alannesta/lister-reporting --build-arg JAR_FILE=target/lister-report-0.1.0.jar .
FROM openjdk:8-jdk-alpine
VOLUME /tmp
ARG JAR_FILE
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]