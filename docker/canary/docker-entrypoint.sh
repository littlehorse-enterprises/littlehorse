#!/bin/bash

set -e

if [ "$1" = 'canary' ]; then
    shift
    exec java $JAVA_OPTS -jar /lh/canary.jar "$@"
fi

exec "$@"
