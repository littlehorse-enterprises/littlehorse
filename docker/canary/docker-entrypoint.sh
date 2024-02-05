#!/bin/bash

set -e

if [ "$1" = 'canary' ]; then
    shift
    exec java -Dlogback.configurationFile=/lh/logback.xml -jar /lh/canary.jar "$@"
fi

exec "$@"
