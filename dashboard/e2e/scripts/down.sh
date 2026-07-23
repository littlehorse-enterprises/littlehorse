#!/usr/bin/env bash
# Tear down the local E2E stack started by up.sh.
set -uo pipefail

FIXTURES_PID="/tmp/dashe2e-fixtures.pid"

if [ -f "${FIXTURES_PID}" ]; then
  kill "$(cat "${FIXTURES_PID}")" 2>/dev/null || true
  rm -f "${FIXTURES_PID}"
fi
docker rm -f lh-e2e >/dev/null 2>&1 || true
echo "==> E2E stack stopped."
