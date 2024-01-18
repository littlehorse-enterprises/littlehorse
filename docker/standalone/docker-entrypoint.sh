#!/bin/bash

set -e

/lh/kafka-entrypoint.sh &

if ! kafka-topics.sh --bootstrap-server=localhost:9092 --list > /dev/null 2>&1; then
    echo "Error trying to connect to kafka, exiting..."
    exit 1
fi

#/lh/dashboard-entrypoint.sh &

/lh/littlehorse-entrypoint.sh
