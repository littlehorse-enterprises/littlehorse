#!/bin/bash

set -e

export API_URL=${API_URL:-localhost:2023}
export AUTHENTICATION_ENABLED=${AUTHENTICATION_ENABLED:-"false"}
export AUTH_SECRET=${AUTH_SECRET:-$(uuidgen)}
export NEXTAUTH_URL=${AUTH_CALLBACK_URL:-http://localhost:8080/}

cd /lh/dashboard

pnpm run build
pnpm start