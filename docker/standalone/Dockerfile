FROM ubuntu:24.04

LABEL maintainer="engineering@littlehorse.io"

ARG KAFKA_VERSION=4.1.0

ENV PATH=${PATH}:/kafka/bin:/lh
ENV LHD_API_HOST=localhost
ENV LHD_API_PORT=2023
ENV DASHBOARD_NEXT=true
ENV NODE_ENV=production

RUN mkdir /kafka /lh
RUN apt update && apt install -y tar gzip unzip wget uuid-runtime openjdk-21-jdk curl ca-certificates gnupg nodejs && apt clean
RUN wget -q https://dlcdn.apache.org/kafka/${KAFKA_VERSION}/kafka_2.13-${KAFKA_VERSION}.tgz -O /tmp/kafka.tgz \
    && tar -xzf /tmp/kafka.tgz --strip-components 1 -C /kafka \
    && rm /tmp/kafka.tgz

COPY ./docker/standalone/kafka-entrypoint.sh /lh
COPY ./docker/standalone/littlehorse-entrypoint.sh /lh
COPY ./docker/standalone/dashboard-entrypoint.sh /lh
COPY ./docker/standalone/docker-entrypoint.sh /lh
COPY ./dashboard/.next/standalone /lh/dashboard
COPY ./dashboard/.next/static /lh/dashboard/.next/static
COPY ./server/build/libs/server-*-all.jar /lh/server.jar

ENTRYPOINT ["/lh/docker-entrypoint.sh"]
