FROM node:16.14-alpine

COPY ./dashboard /lh/dashboard
WORKDIR /lh/dashboard

RUN npm i -g pnpm
RUN pnpm install && pnpm run build

CMD ["pnpm", "start"]
