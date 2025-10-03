#!/bin/sh

set -e

if [ "$1" = 'canary' ]; then
    shift
    exec /lh/bin/canary "$@"
fi

exec "$@"
