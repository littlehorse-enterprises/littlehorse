FROM node:16.14-alpine

RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app
COPY . .

RUN npm i -g pnpm 
RUN pnpm install \
    && pnpm run build
EXPOSE 80
CMD ["pnpm", "start"]

