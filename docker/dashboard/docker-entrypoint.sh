#!/bin/bash

set -e

if [ "$1" = 'dashboard' ]; then
    shift

    if [ -z "$API_URL" ]; then
      echo "Provide the API_URL env variable"
      exit 1
    fi

    if [ -z "$AUTHENTICATION_ENABLED" ]; then
      echo "Provide the AUTHENTICATION_ENABLED env variable"
      exit 1
    fi

    export AUTH_SECRET=${AUTH_SECRET:-$(uuidgen)}
    export NEXTAUTH_URL=${AUTH_CALLBACK_URL:-http://localhost:8080/}

    pnpm run build
    pnpm start
fi

exec "$@"