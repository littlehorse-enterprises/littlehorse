#!/bin/bash

set -e

if [ "$1" = 'server' ]; then
    shift
    exec java -jar /lh/app.jar "$@"
fi

exec "$@"
