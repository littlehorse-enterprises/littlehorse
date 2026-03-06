# LittleHorse Examples

This directory contains runnable examples of LittleHorse workflows and workers across multiple SDKs.

## Run LittleHorse Locally

Pick one of these local setups before running examples.

### Option 1: Standalone container (quickest)

```bash
docker run --rm --pull=always --name littlehorse -d \
  -p 9092:9092 -p 2023:2023 -p 8080:8080 \
  ghcr.io/littlehorse-enterprises/littlehorse/lh-standalone:latest
```

### Option 2: Local development scripts

```bash
./local-dev/setup.sh
./local-dev/do-server.sh
```

### Verify connectivity

```bash
lhctl whoami
```

If that succeeds, examples can connect to `localhost:2023`.

### Option: Remote LittleHorse

All of the examples are configured to use the `~/.config/littlehorse.config` file (if it exists) as configuration. If the file is empty or does not exist then it defaults to `localhost:2023`, which is what the above setup does. However if you want to use a remote LH Cluster (eg. LittleHorse Cloud) you can edit that file and it will work.

## Example Directory Index

- [`java/`](./java/README.md): Java SDK examples, runnable via Gradle.
- [`python/`](./python/README.md): Python SDK examples, runnable with `poetry`/`python`.
- [`go/`](./go/README.md): Go SDK examples, generally split into worker/deploy steps.
- [`dotnet/`](./dotnet/README.md): .NET SDK examples, runnable with `dotnet run`.
- [`js/`](./js/README.md): JavaScript/TypeScript SDK worker examples.
- [`docker-compose/`](./docker-compose/README.md): Docker Compose sandbox environments.

## Common Workflow

1. Start LittleHorse locally using one option above.
2. Run one example from a subdirectory README.
3. In another terminal, execute `lhctl run ...` when the example asks for it.
4. Inspect runs with `lhctl get wfRun <id>` or the dashboard.
