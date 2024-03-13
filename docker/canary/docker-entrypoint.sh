#!/bin/bash

set -e

if [ "$1" = 'canary' ]; then
    shift
    exec java -Dlog4j2.configurationFile=/lh/log4j2.properties -jar /lh/canary.jar "$@"
fi

exec "$@"
