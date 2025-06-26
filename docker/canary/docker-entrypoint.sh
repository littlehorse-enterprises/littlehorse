#!/bin/bash

set -e

if [ "$1" = 'canary' ]; then
    shift
    exec java $JAVA_OPTS -jar /lh/java/canary.jar "$@"
fi

exec "$@"
