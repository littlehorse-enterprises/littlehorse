FROM ghcr.io/littlehorse-enterprises/alpine-nginx-nodejs/nginx-nodejs:main as runner
ENV NODE_ENV production

RUN mkdir /app
WORKDIR /app
RUN mkdir .next

COPY ./dashboard/.next/standalone ./
COPY ./dashboard/.next/static ./.next/static

EXPOSE 3000
ENV PORT 3000
ENV HOSTNAME "0.0.0.0"
