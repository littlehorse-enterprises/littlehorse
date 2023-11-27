#!/bin/bash

set -e

if [ "$1" = 'dashboard' ]; then
    shift
    pnpm run build
    pnpm start
fi

exec "$@"