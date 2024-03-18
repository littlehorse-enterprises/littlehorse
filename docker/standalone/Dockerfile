FROM ubuntu:22.04

LABEL maintainer="engineering@littlehorse.io"

ENV PATH ${PATH}:/kafka/bin:/lh

RUN mkdir /kafka /lh \
    && apt update && apt install -y tar gzip wget uuid-runtime openjdk-17-jdk curl ca-certificates gnupg \
    && curl -fsSL https://deb.nodesource.com/gpgkey/nodesource-repo.gpg.key | gpg --dearmor -o /etc/apt/keyrings/nodesource.gpg \
    && echo "deb [signed-by=/etc/apt/keyrings/nodesource.gpg] https://deb.nodesource.com/node_20.x nodistro main" | tee /etc/apt/sources.list.d/nodesource.list \
    && apt update && apt install -y nodejs \
    && npm i -g pnpm \
    && rm -rf /var/lib/apt/lists/* \
    && wget -q https://downloads.apache.org/kafka/3.7.0/kafka_2.12-3.7.0.tgz -O /tmp/kafka.tgz \
    && tar -xzf /tmp/kafka.tgz --strip-components 1 -C /kafka \
    && rm /tmp/kafka.tgz

COPY ./docker/standalone/kafka-entrypoint.sh /lh
COPY ./docker/standalone/littlehorse-entrypoint.sh /lh
COPY ./docker/standalone/dashboard-entrypoint.sh /lh
COPY ./docker/standalone/docker-entrypoint.sh /lh

WORKDIR /lh/dashboard
ENV NODE_ENV=production
EXPOSE 8080

COPY ./dashboard/apps/web/.next/standalone ./
COPY ./dashboard/apps/web/.next/static ./apps/web/.next/static
COPY ./dashboard/apps/web/public ./apps/web/public

WORKDIR /

COPY ./server/build/libs/server-*-all.jar /lh/server.jar

ENV LHD_API_HOST=localhost
ENV LHD_API_PORT=2023

ENTRYPOINT ["/lh/docker-entrypoint.sh"]
