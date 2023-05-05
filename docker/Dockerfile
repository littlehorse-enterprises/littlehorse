FROM amazoncorretto:17
WORKDIR /lh
COPY app/build/libs/app-0.0.0-ALPHA-all.jar /lh/app.jar
COPY docker/docker-entrypoint.sh /lh/docker-entrypoint.sh
ENTRYPOINT ["/lh/docker-entrypoint.sh"]
CMD ["server", "/lh/server.config"]
