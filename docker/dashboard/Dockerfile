FROM node:16.14-alpine

COPY ./dashboard /lh/dashboard
COPY ./docker/dashboard/docker-entrypoint.sh /lh/dashboard
WORKDIR /lh/dashboard

RUN npm i -g pnpm
RUN pnpm install

ENTRYPOINT ["/lh/dashboard/docker-entrypoint.sh"]
CMD ["dashboard"]