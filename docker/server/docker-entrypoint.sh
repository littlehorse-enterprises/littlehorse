#!/bin/bash

set -e

if [ "$1" = 'server' ]; then
    shift
    exec java $JAVA_OPTS -jar /lh/server.jar "$@"
fi

exec "$@"
