#!/bin/bash

set -e

# Uses jemalloc, which is much better for RocksDB memory management
export LD_PRELOAD="/usr/lib/x86_64-linux-gnu/libjemalloc.so"

if [ "$1" = 'server' ]; then
    shift
    exec java -Dlog4j2.configurationFile=/lh/log4j2.properties -jar /lh/server.jar "$@"
fi

exec "$@"
