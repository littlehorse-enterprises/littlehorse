FROM gradle:8 as builder
WORKDIR /lh
COPY . /lh
RUN gradle server:shadowJar -x test

FROM amazoncorretto:17

LABEL maintainer="engineering@littlehorse.io"

ENV PATH ${PATH}:/kafka/bin:/lh

RUN mkdir /kafka /lh \
    && yum install -y tar gzip wget \
    && yum clean all \
    && wget -q https://dlcdn.apache.org/kafka/3.4.1/kafka_2.13-3.4.1.tgz -O /tmp/kafka.tgz \
    && tar -xzf /tmp/kafka.tgz --strip-components 1 -C /kafka \
    && rm /tmp/kafka.tgz

COPY ./docker/lh-standalone/kafka-entrypoint.sh /lh
COPY ./docker/lh-standalone/littlehorse-entrypoint.sh /lh
COPY ./docker/lh-standalone/docker-entrypoint.sh /lh
COPY ./docker/lh-standalone/log4j2.properties /lh

COPY --from=builder /lh/server/build/libs/server-*-all.jar /lh/server.jar

ENTRYPOINT ["/lh/docker-entrypoint.sh"]
