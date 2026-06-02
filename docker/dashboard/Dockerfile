FROM ghcr.io/littlehorse-enterprises/alpine-nginx-nodejs/nginx-nodejs:v1 AS runner

ENV NODE_ENV=production
ENV PORT=3000
ENV HOSTNAME="0.0.0.0"

RUN apk add --no-cache uuidgen

RUN mkdir /app
WORKDIR /app
RUN mkdir .next

COPY ./dashboard/.next/standalone ./
COPY ./dashboard/.next/static ./.next/static

COPY ./docker/dashboard/entrypoint.sh ./entrypoint.sh
EXPOSE 3000

ENTRYPOINT [ "/app/entrypoint.sh" ]
