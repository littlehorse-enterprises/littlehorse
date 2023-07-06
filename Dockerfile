FROM node:16.14-alpine

RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app
COPY . .

COPY .env apps/web/.env
# --no-cache: download package index on-the-fly, no need to cleanup afterwards
# --virtual: bundle packages, remove whole bundle at once, when done
RUN npm i -g pnpm 
RUN pnpm install \
    && pnpm run build
EXPOSE 3001
# CMD [ "node", "dist/main" ]
CMD ["pnpm", "start"]
#

# docker build -t lhd .
# docker run -p 3003:3001 -d lhd 
