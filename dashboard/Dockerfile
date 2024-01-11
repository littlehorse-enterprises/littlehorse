FROM node:20-alpine AS base

# 1. Install dependencies only when needed
FROM base AS deps
# Check https://github.com/nodejs/docker-node/tree/b4117f9333da4138b03a546ec926ef50a31506c3#nodealpine to understand why libc6-compat might be needed.
RUN apk add --no-cache libc6-compat

WORKDIR /app

# Install dependencies based on the preferred package manager
COPY package.json pnpm-lock.yaml* ./
RUN npm i -g pnpm && npm i -g turbo && pnpm i

# 2. Rebuild the source code only when needed
FROM base AS builder
WORKDIR /app
RUN npm i -g pnpm
RUN npm i -g turbo

COPY --from=deps /app/node_modules ./node_modules
COPY . .

RUN turbo prune web --docker

ENV NEXT_PRIVATE_STANDALONE=true
# This will do the trick, use the corresponding env file for each environment.
#COPY .env.sample .env.production
RUN pnpm run build

# 3. Production image, copy all the files and run next
FROM base AS runner
WORKDIR /app
RUN npm i -g pnpm
ENV NODE_ENV=production

RUN addgroup -g 1001 -S nodejs
RUN adduser -S nextjs -u 1001
USER nextjs

COPY --from=builder --chown=nextjs:nodejs /app/apps/web/next.config.js .
COPY --from=builder --chown=nextjs:nodejs /app/apps/web/package.json .

COPY --from=builder --chown=nextjs:nodejs /app/apps/web/.next/standalone ./
COPY --from=builder --chown=nextjs:nodejs /app/apps/web/.next/static ./apps/web/.next/static
COPY --from=builder --chown=nextjs:nodejs /app/apps/web/public ./apps/web/public

EXPOSE 3000

ENV PORT 3000
ENV HOSTNAME localhost

CMD ["node", "apps/web/server.js"]
