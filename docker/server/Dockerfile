FROM amazoncorretto:17
WORKDIR /lh
COPY ./docker/server/docker-entrypoint.sh /lh
COPY ./server/build/libs/server-*-all.jar /lh/server.jar
ENTRYPOINT ["/lh/docker-entrypoint.sh"]
CMD ["server"]
