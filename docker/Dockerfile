FROM amazoncorretto:17
WORKDIR /lh
COPY server/build/libs/server-*-all.jar /lh/server.jar
COPY docker/docker-entrypoint.sh /lh/docker-entrypoint.sh
ENTRYPOINT ["/lh/docker-entrypoint.sh"]
CMD ["server", "/lh/server.config"]
