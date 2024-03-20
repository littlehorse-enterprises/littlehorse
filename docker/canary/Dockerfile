FROM amazoncorretto:21
WORKDIR /lh
COPY ./docker/canary/docker-entrypoint.sh /lh
COPY ./docker/canary/log4j2.properties /lh
COPY ./canary/build/libs/canary-*-all.jar /lh/canary.jar
ENTRYPOINT ["/lh/docker-entrypoint.sh"]
CMD ["canary"]
