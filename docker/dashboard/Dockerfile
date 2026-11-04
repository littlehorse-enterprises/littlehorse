FROM node:24-alpine AS runner

RUN apk add --no-cache nginx supervisor uuidgen
COPY ./docker/dashboard/nginx.conf /etc/nginx/nginx.conf
COPY ./docker/dashboard/supervisord.conf /etc/supervisord.conf
COPY ./docker/dashboard/supervisor.d /etc/supervisor.d

ENV NODE_ENV=production
ENV PORT=3000
ENV HOSTNAME="0.0.0.0"

RUN mkdir /app
WORKDIR /app

# `outputFileTracingRoot` is the repository root, so the standalone build is emitted as
# `.next/standalone/{dashboard,sdk-js}/` rather than flat. Keep that shape -- the traced
# paths inside server.js are relative to it -- and point the `node /app/server.js` run by
# entrypoint.sh and supervisor.d at the real entrypoint. server.js does
# `process.chdir(__dirname)` and node resolves the main module through the symlink, so it
# runs from /app/dashboard.
COPY ./dashboard/.next/standalone ./
COPY ./dashboard/.next/static ./dashboard/.next/static
RUN ln -s /app/dashboard/server.js /app/server.js

COPY ./docker/dashboard/entrypoint.sh ./entrypoint.sh
EXPOSE 3000 3443

ENTRYPOINT [ "/app/entrypoint.sh" ]
