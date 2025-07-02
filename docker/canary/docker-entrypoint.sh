#!/bin/bash

set -e

if [ "$1" = 'canary' ]; then
    shift
    exec java $JAVA_OPTS -jar /lh/core/canary.jar "$@"
fi

if [ "$1" = 'dotnet-worker' ]; then
    shift
    exec dotnet /lh/dotnet/LittleHorse.Canary.Worker.dll "$@"
fi

exec "$@"
