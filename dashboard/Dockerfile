FROM node:20-alpine AS base

# 1. Install dependencies only when needed
FROM base AS deps
# Check https://github.com/nodejs/docker-node/tree/b4117f9333da4138b03a546ec926ef50a31506c3#nodealpine to understand why libc6-compat might be needed.
RUN apk add --no-cache libc6-compat

WORKDIR /app

# Install dependencies based on the preferred package manager
COPY package.json pnpm-lock.yaml* ./
RUN npm i -g pnpm && pnpm i

# 2. Rebuild the source code only when needed
FROM base AS builder
WORKDIR /app
RUN npm i -g pnpm

COPY --from=deps /app/node_modules ./node_modules
COPY . .

ENV NEXT_PRIVATE_STANDALONE=true
# This will do the trick, use the corresponding env file for each environment.
#COPY .env.production.sample .env.production
RUN pnpm run build

# 3. Production image, copy all the files and run next
FROM base AS runner
WORKDIR /app
RUN npm i -g pnpm
ENV NODE_ENV=production

RUN addgroup -g 1001 -S nodejs
RUN adduser -S nextjs -u 1001

COPY --from=builder --chown=nextjs:nodejs /app/apps/web/next.config.js ./
COPY --from=builder --chown=nextjs:nodejs /app/apps/web/public ./public
#COPY --from=builder --chown=nextjs:nodejs /app/apps/web/.next ./_next

# Automatically leverage output traces to reduce image size
# https://nextjs.org/docs/advanced-features/output-file-tracing
COPY --from=builder --chown=nextjs:nodejs /app/apps/web/.next/standalone ./
COPY --from=builder --chown=nextjs:nodejs /app/apps/web/.next/static ./_next/static


USER nextjs

EXPOSE 3000

ENV PORT 3000
ENV HOSTNAME localhost

CMD ["node", "apps/web/server.js"]
