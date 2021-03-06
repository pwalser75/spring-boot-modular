FROM adoptopenjdk/openjdk11:latest
MAINTAINER frostnova.ch
COPY ./app/build/libs/app-1.0.0-SNAPSHOT.jar /opt/app/app.jar
WORKDIR /opt/app
EXPOSE 80
EXPOSE 443
ENTRYPOINT ["java", "-server", "-Djava.security.egd=file:/dev/./urandom","-jar","app.jar", "--spring.config.location=/config/application.yml"]

