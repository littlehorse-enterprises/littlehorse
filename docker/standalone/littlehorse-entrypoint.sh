#!/bin/bash

set -e

export LHS_REPLICATION_FACTOR=${LHS_REPLICATION_FACTOR:-1}
export LHS_CLUSTER_PARTITIONS=${LHS_CLUSTER_PARTITIONS:-12}
export LHS_STATE_DIR=${LHS_STATE_DIR:-/data/lh}
export LHS_SHOULD_CREATE_TOPICS=${LHS_SHOULD_CREATE_TOPICS:-true}

exec java -Dlog4j2.configurationFile=/lh/log4j2.properties -jar /lh/server.jar
