#!/bin/bash

set -e

if [ "$1" = 'server' ]; then
    shift
    exec /lh/bin/server "$@"
fi

exec "$@"
