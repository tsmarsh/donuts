FROM tsmarsh/lein

RUN mkdir -p /usr/src/games
WORKDIR /usr/src/games

COPY project.clj /usr/src/games/project.clj
RUN lein deps

COPY . /usr/src/games
RUN lein uberjar

FROM openjdk:17-slim

EXPOSE 8888

ENV DB_NAME=docker
ENV DB_ADDRESS=docker:27017

COPY --from=0 /usr/src/games/config.edn /usr/src/games/config.edn
COPY --from=0 /usr/src/games/target/*standalone.jar /usr/src/games/server.jar
WORKDIR /usr/src/games

CMD ["java", "-jar", "server.jar" ]
