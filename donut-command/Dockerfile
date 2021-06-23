FROM maven:3.8.1-openjdk-17

RUN mkdir -p /usr/src/donut
WORKDIR /usr/src/donut

COPY pom.xml /usr/src/donut/pom.xml
RUN mvn dependency:resolve

COPY . /usr/src/donut
RUN mvn package

RUN ls target

FROM openjdk:17-slim

EXPOSE 8888

ENV DB_NAME=docker
ENV DB_ADDRESS=docker:27017
ENV DB_NAME=donut
ENV PORT=8888

COPY --from=0 /usr/src/donut/target/*-jar-with-dependencies.jar /usr/src/donut/server.jar
WORKDIR /usr/src/donut

CMD ["java", "-jar", "server.jar" ]