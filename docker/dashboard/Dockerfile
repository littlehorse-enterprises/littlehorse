FROM ghcr.io/littlehorse-enterprises/alpine-nginx-nodejs/nginx-nodejs:v1 as runner
ENV NODE_ENV production

RUN apk add --no-cache uuidgen

RUN mkdir /app
WORKDIR /app
RUN mkdir .next

COPY ./dashboard/.next/standalone ./
COPY ./dashboard/.next/static ./.next/static

COPY ./docker/dashboard/entrypoint.sh ./entrypoint.sh
EXPOSE 3000
ENV PORT 3000
ENV HOSTNAME "0.0.0.0"

ENTRYPOINT [ "/app/entrypoint.sh" ]
