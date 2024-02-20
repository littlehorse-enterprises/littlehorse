#!/bin/bash

set -e

# SET CONTEXT

SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)
CONTEXT_DIR=$(cd "$SCRIPT_DIR/.." && pwd)

cd "${CONTEXT_DIR}"

# BUILD DASHBOARD IF REQUESTED

dashboard=false

while [[ $# -gt 0 ]]; do
  case "$1" in
    --dashboard)
      dashboard=true
      ;;
    *)
      echo "Unknown argument: $1"
      exit 1
      ;;
  esac
  shift
done

if [ ${dashboard} = true ]; then
    cd dashboard
    pnpm install
    pnpm build
    cd ..
    docker build -t littlehorse/lh-dashboard:latest -f docker/dashboard/Dockerfile .
    exit 0
fi

# BY DEFAULT BUILD LH SERVER DOCKER

echo "Building server image using host machine's gradle cache"
./gradlew server:shadowJar -x test -x spotlessJavaCheck
docker build -t littlehorse/lh-server:latest -f docker/server/Dockerfile .
