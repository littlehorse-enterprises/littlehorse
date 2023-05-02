#!/bin/bash

set -e

SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)
WORK_DIR=$SCRIPT_DIR

kafka_topics_sh="docker compose --file $WORK_DIR/docker-compose.yml
    --project-directory $WORK_DIR
    --project-name lh-server-local-dev
    exec kafka bash /opt/bitnami/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092"

TOPICS=$($kafka_topics_sh --list)

if [ -n "$TOPICS" ]; then
    echo "Topics to be deleted:"
    echo "$TOPICS"
    $kafka_topics_sh --delete --topic ".*"
fi

rm -rf /tmp/kafkaState*
