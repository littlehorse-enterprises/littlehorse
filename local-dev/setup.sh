#!/bin/bash

set -e

SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)
WORK_DIR=$SCRIPT_DIR

docker compose --file "$WORK_DIR/docker-compose.yml" \
    --project-directory "$WORK_DIR" \
    --project-name lh-server-local-dev \
    up -d

echo
echo "Kafka port: 9092"
