FROM gradle:8 as builder
WORKDIR /lh
COPY . /lh
RUN gradle server:shadowJar -x test

FROM amazoncorretto:17
RUN mkdir /lh
COPY ./docker/server/docker-entrypoint.sh /lh
COPY ./docker/server/log4j2.properties /lh
COPY --from=builder /lh/server/build/libs/server-*-all.jar /lh/server.jar
ENTRYPOINT ["/lh/docker-entrypoint.sh"]
CMD ["server"]
