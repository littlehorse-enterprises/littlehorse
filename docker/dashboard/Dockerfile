FROM node:20 AS base
RUN apt-get update && apt-get install -y uuid-runtime \
  && rm -rf /var/lib/apt/lists/*

FROM base AS runner
WORKDIR /lh/dashboard
ENV NODE_ENV=production

RUN groupadd -g 1001 nodejs
RUN useradd nextjs -u 1001
USER nextjs

COPY --chown=nextjs:nodejs ./dashboard/apps/web/.next/standalone ./
COPY --chown=nextjs:nodejs ./dashboard/apps/web/.next/static ./apps/web/.next/static
COPY --chown=nextjs:nodejs ./dashboard/apps/web/public ./apps/web/public

COPY ./docker/dashboard/docker-entrypoint.sh ./

EXPOSE 8080

ENV PORT 8080
ENV HOSTNAME 0.0.0.0

ENTRYPOINT ["/lh/dashboard/docker-entrypoint.sh"]
CMD ["dashboard"]
