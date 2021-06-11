FROM maven:3.8.1-openjdk-17

RUN mkdir -p /usr/src/donut
WORKDIR /usr/src/donut

COPY pom.xml /usr/src/games/pom.xml
RUN mvn dependency:resolve

COPY . /usr/src/donut
RUN mvn package

FROM openjdk:17-slim

EXPOSE 8888

ENV DB_NAME=docker
ENV DB_ADDRESS=docker:27017
ENV DB_NAME=donut

COPY --from=0 /usr/src/games/target/*standalone.jar /usr/src/games/server.jar
WORKDIR /usr/src/games

CMD ["java", "-jar", "server.jar" ]