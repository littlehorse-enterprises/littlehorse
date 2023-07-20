FROM ubuntu:22.04 as builder
WORKDIR /lh
RUN apt-get update && apt-get install -y openjdk-11-jdk-headless openjdk-17-jdk-headless
COPY . /lh
RUN ./gradlew server:shadowJar -x test

FROM amazoncorretto:17
WORKDIR /lh
COPY --from=builder /lh/server/build/libs/server-*-all.jar /lh/server.jar
COPY --from=builder /lh/docker/docker-entrypoint.sh /lh/docker-entrypoint.sh
COPY --from=builder /lh/docker/log4j2.properties /lh/log4j2.properties
ENTRYPOINT ["/lh/docker-entrypoint.sh"]
CMD ["server"]
