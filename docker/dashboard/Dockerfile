FROM node:20

COPY ./dashboard /lh/dashboard
COPY ./docker/dashboard/docker-entrypoint.sh /lh/dashboard
WORKDIR /lh/dashboard

RUN npm i -g pnpm
RUN pnpm install --force

ENTRYPOINT ["/lh/dashboard/docker-entrypoint.sh"]
CMD ["dashboard"]