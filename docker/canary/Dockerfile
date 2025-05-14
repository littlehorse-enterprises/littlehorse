FROM amazoncorretto:21.0.6
WORKDIR /lh
COPY ./docker/canary/docker-entrypoint.sh /lh
COPY ./canary/build/libs/canary-*-all.jar /lh/canary.jar
ENTRYPOINT ["/lh/docker-entrypoint.sh"]
CMD ["canary"]
