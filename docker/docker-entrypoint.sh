#!/bin/bash

set -e

if [ "$1" = 'server' ]; then
    shift
    exec java -jar /lh/server.jar "$@"
fi

exec "$@"
