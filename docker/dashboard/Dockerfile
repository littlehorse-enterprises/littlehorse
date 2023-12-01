FROM node:20

COPY ./dashboard /lh/dashboard
COPY ./docker/dashboard/docker-entrypoint.sh /lh/dashboard
WORKDIR /lh/dashboard

RUN apt-get update && apt-get install -y \
  uuid-runtime \
  && rm -rf /var/lib/apt/lists/* \
  && npm i -g pnpm \
  && pnpm install --force

ENTRYPOINT ["/lh/dashboard/docker-entrypoint.sh"]
CMD ["dashboard"]