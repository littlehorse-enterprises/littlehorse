FROM amazoncorretto:21-alpine3.21

RUN apk add aspnetcore8-runtime librdkafka \
    && ln -s /usr/lib/librdkafka.so.1 /usr/lib/alpine-librdkafka.so

WORKDIR /lh
COPY ./docker/canary/docker-entrypoint.sh /lh/java
COPY ./canary/build/libs/canary-*-all.jar /lh/java/canary.jar
ENTRYPOINT ["/lh/docker-entrypoint.sh"]
CMD ["canary"]
