#!/bin/bash

set -e

SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)
WORK_DIR=$(cd "$SCRIPT_DIR/.." && pwd)

cd "$WORK_DIR"

# Checks if dotnet is installed
command -v dotnet >/dev/null 2>&1 || { echo >&2 "dotnet version 8 is required but it's not installed.  Aborting."; exit 1; }

# Run Core Canary (Aggregator, Metronome and Metronome Java Worker)
./gradlew canary:installDist
./canary/core/build/install/canary/bin/canary canary/core/canary.properties &
CORE_PID=$!

# Run .NET Metronome Worker
dotnet run --project canary/dotnet-worker/LittleHorse.Canary.Worker/LittleHorse.Canary.Worker.csproj &
DOTNET_WORKER_PID=$!

cleanup() {
    kill "$CORE_PID"
    kill "$DOTNET_WORKER_PID"
    exit 0
}

# Stops both processes cleanly on ctrl+c or terminal closed
trap cleanup INT

wait
