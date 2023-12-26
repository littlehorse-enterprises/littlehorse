#!/bin/bash

set -e

export API_URL="localhost:2023"
export AUTHENTICATION_ENABLED="false"
export AUTH_SECRET="$(uuidgen)"

cd /lh/dashboard

pnpm run build
pnpm start
