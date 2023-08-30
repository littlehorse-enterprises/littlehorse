#!/bin/bash

set -e

SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)
WORK_DIR=$(cd "$SCRIPT_DIR/.." && pwd)
CONFIG_NAME="server-1"
DOCKER=false
LH_SERVER_DOCKER_DIR=$(cd "$SCRIPT_DIR/../docker/server" && pwd)
CERT_PATH="${SCRIPT_DIR}/certs"

if [ "$1" == "--docker" ]; then
  shift
  DOCKER=true
fi

if [ -n "$1" ]; then
  CONFIG_NAME="$1"
fi

CONFIG_PATH="${SCRIPT_DIR}/configs/${CONFIG_NAME}.config"

if [ ! -f "$CONFIG_PATH" ]; then
  echo "$CONFIG_PATH does not exist."
  exit 1
fi

cd "$WORK_DIR"

if $DOCKER; then
  echo "Building server"
  docker build -q --file ${LH_SERVER_DOCKER_DIR}/Dockerfile --tag littlehorse/littlehorse-server:latest ${WORK_DIR}
  echo "Host config file: $CONFIG_PATH"
  docker run --rm -it --network host --volume "$CONFIG_PATH":/lh/server.config --volume "$CERT_PATH":/local-dev/certs littlehorse/littlehorse-server:latest server /lh/server.config
else
  ./gradlew server:installDist -x shadowJar -x test
  ./server/build/install/server/server/server "$CONFIG_PATH"
fi
