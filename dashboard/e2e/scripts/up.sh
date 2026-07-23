#!/usr/bin/env bash
# Bring up the local E2E stack: a LittleHorse standalone server + seeded fixtures.
# Usage: pnpm e2e:up  (then `pnpm e2e`, then `pnpm e2e:down`)
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "$0")/../../.." && pwd)"
LH_TAG="${LH_TAG:-master}"
LH_IMAGE="ghcr.io/littlehorse-enterprises/littlehorse/lh-standalone:${LH_TAG}"
FIXTURES_LOG="/tmp/dashe2e-fixtures.log"
FIXTURES_PID="/tmp/dashe2e-fixtures.pid"

echo "==> Starting LittleHorse standalone (${LH_IMAGE})..."
docker rm -f lh-e2e >/dev/null 2>&1 || true
docker run --rm -d --name lh-e2e -p 2023:2023 "${LH_IMAGE}" >/dev/null
until nc -z localhost 2023 2>/dev/null; do sleep 1; done

echo "==> Seeding deterministic fixtures..."
cd "${REPO_ROOT}"
LHC_API_HOST=localhost LHC_API_PORT=2023 \
  nohup ./gradlew :example-dashboard-e2e-fixtures:run --console=plain >"${FIXTURES_LOG}" 2>&1 &
echo $! >"${FIXTURES_PID}"

until grep -q FIXTURES_READY "${FIXTURES_LOG}" 2>/dev/null; do
  if ! kill -0 "$(cat "${FIXTURES_PID}")" 2>/dev/null; then
    echo "!! Fixture seeder exited before it was ready:" >&2
    tail -30 "${FIXTURES_LOG}" >&2
    exit 1
  fi
  sleep 2
done

echo "==> Stack is up. LittleHorse on :2023, fixtures seeded. Now run: pnpm e2e"
