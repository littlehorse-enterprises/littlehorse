FROM amazoncorretto:21-alpine3.21

RUN apk add aspnetcore8-runtime librdkafka \
    && ln -s /usr/lib/librdkafka.so.1 /usr/lib/alpine-librdkafka.so

WORKDIR /lh

COPY ./docker/canary/docker-entrypoint.sh /lh
COPY ./canary/core/build/libs/canary-*-all.jar /lh/core/canary.jar
COPY ./canary/dotnet-worker/LittleHorse.Canary.Worker/bin/Release/net8.0 /lh/dotnet

ENTRYPOINT ["sh"]
CMD ["/lh/docker-entrypoint.sh", "canary"]
