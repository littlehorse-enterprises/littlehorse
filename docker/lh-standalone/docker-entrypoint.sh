#!/bin/bash

set -e

/lh/kafka-entrypoint.sh &

RETRIES=2

until kafka-topics.sh --bootstrap-server=localhost:9092 --list > /dev/null 2>&1 || [[ $RETRIES == 0 ]]; do
    echo "Waiting for kafka, $((RETRIES--)) remaining attempts..."
    sleep 1
done

/lh/littlehorse-entrypoint.sh
