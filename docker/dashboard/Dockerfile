FROM node:20 AS base
RUN apt-get update && apt-get install -y uuid-runtime \
  && rm -rf /var/lib/apt/lists/*

# 1. Install dependencies only when needed
FROM base AS deps

WORKDIR /lh/dashboard

COPY ./dashboard/package.json ./
COPY ./dashboard/pnpm-lock.yaml* ./

# Install dependencies based on the preferred package manager
RUN npm i -g pnpm && pnpm i

# 2. Rebuild the source code only when needed
FROM base AS builder
WORKDIR /lh/dashboard

RUN npm i -g pnpm
RUN npm i -g turbo

COPY --from=deps /lh/dashboard/node_modules ./node_modules
COPY ./dashboard ./

RUN turbo prune web --docker

ENV NEXT_PRIVATE_STANDALONE=true
# This will do the trick, use the corresponding env file for each environment.
#COPY .env.sample .env.production
RUN pnpm run build

# 3. Production image, copy all the files and run next
FROM base AS runner
WORKDIR /lh/dashboard
RUN npm i -g pnpm
ENV NODE_ENV=production

RUN groupadd -g 1001 nodejs
RUN useradd nextjs -u 1001
USER nextjs

COPY --from=builder --chown=nextjs:nodejs /lh/dashboard/apps/web/next.config.js .
COPY --from=builder --chown=nextjs:nodejs /lh/dashboard/apps/web/package.json .

COPY --from=builder --chown=nextjs:nodejs /lh/dashboard/apps/web/.next/standalone ./
COPY --from=builder --chown=nextjs:nodejs /lh/dashboard/apps/web/.next/static ./apps/web/.next/static
COPY --from=builder --chown=nextjs:nodejs /lh/dashboard/apps/web/public ./apps/web/public

COPY ./docker/dashboard/docker-entrypoint.sh ./

EXPOSE 8080

ENV PORT 8080
ENV HOSTNAME localhost

ENTRYPOINT ["/lh/dashboard/docker-entrypoint.sh"]
CMD ["dashboard"]
