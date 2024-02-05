FROM amazoncorretto:17
WORKDIR /lh
COPY ./docker/canary/docker-entrypoint.sh /lh
COPY ./docker/canary/logback.xml /lh
COPY ./canary/build/libs/canary-*-all.jar /lh/canary.jar
ENTRYPOINT ["/lh/docker-entrypoint.sh"]
CMD ["canary"]
