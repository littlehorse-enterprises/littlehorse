#!/bin/bash

set -e

SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)
CONTEXT_DIR=$(cd "$SCRIPT_DIR/.." && pwd)

cd "${CONTEXT_DIR}"

dashboard=false
canary=false
server=false
standalone=false

if [[ $# -eq 0 ]]; then
  server=true
fi

while [[ $# -gt 0 ]]; do
  case "$1" in
    --dashboard)
      dashboard=true
      ;;
    --canary)
      canary=true
      ;;
    --standalone)
      standalone=true
      ;;
    --server)
      server=true
      ;;
    *)
      echo "Unknown argument: $1"
      exit 1
      ;;
  esac
  shift
done

if [[ ${dashboard} = true ]]; then
    echo "Building lh-dashboard"
    cd dashboard
    npm install
    npm run build
    cd ..
    docker build -t littlehorse/lh-dashboard:latest -f docker/dashboard/Dockerfile .
fi

if [[ ${canary} = true ]]; then
    echo "Building lh-canary"
    ./gradlew canary:shadowJar -x test -x spotlessJavaCheck
    docker build -t littlehorse/lh-canary:latest -f docker/canary/Dockerfile .
fi

if [[ ${server} = true ]]; then
    echo "Building lh-server"
    ./gradlew server:installDist -x test -x spotlessJavaCheck
    docker build -t littlehorse/lh-server:latest -f docker/server/Dockerfile .
fi

if [[ ${standalone} = true ]]; then
    echo "Building lh-standalone"
    cd dashboard
    npm install
    npm run build
    cd ..
    ./gradlew server:shadowJar -x test -x spotlessJavaCheck
    docker build -t littlehorse/lh-standalone:latest -f docker/standalone/Dockerfile .
fi
