FROM amazoncorretto:25

LABEL maintainer="engineering@littlehorse.io"

ARG KAFKA_VERSION=4.3.1

ENV PATH=${PATH}:/kafka/bin:/lh
ENV LHD_API_HOST=localhost
ENV LHD_API_PORT=2023
ENV DASHBOARD_NEXT=true
ENV NODE_ENV=production

RUN mkdir /kafka /lh

RUN yum install -y bash wget tar gzip nodejs findutils  && \
  yum clean all && \
  rm -rf /var/cache/yum

RUN wget -q https://dlcdn.apache.org/kafka/${KAFKA_VERSION}/kafka_2.13-${KAFKA_VERSION}.tgz -O /tmp/kafka.tgz \
    && tar -xzf /tmp/kafka.tgz --strip-components 1 -C /kafka \
    && rm /tmp/kafka.tgz

COPY ./docker/standalone/kafka-entrypoint.sh /lh
COPY ./docker/standalone/littlehorse-entrypoint.sh /lh
COPY ./docker/standalone/dashboard-entrypoint.sh /lh
COPY ./docker/standalone/docker-entrypoint.sh /lh
# `outputFileTracingRoot` is the repository root, so the standalone build is emitted as
# `.next/standalone/{dashboard,sdk-js}/` rather than flat. Copy it to /lh to keep that
# shape -- the traced paths inside server.js are relative to it -- which lands the real
# entrypoint at the /lh/dashboard/server.js that dashboard-entrypoint.sh already runs.
COPY ./dashboard/.next/standalone /lh
COPY ./dashboard/.next/static /lh/dashboard/.next/static
COPY ./server/build/install/server/lib /lh/lib
COPY ./server/build/install/server/server /lh/bin
RUN ["chmod", "+x", "/lh/bin/server"]

ENTRYPOINT ["/lh/docker-entrypoint.sh"]
