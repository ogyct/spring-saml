FROM --platform=linux/amd64 docker.io/library/openjdk:21-jdk
WORKDIR /app
COPY build/libs/spring-saml-?.?.?-SNAPSHOT.jar /app/app.jar
EXPOSE 8080
CMD ["java", "-jar", "/app/app.jar"]
