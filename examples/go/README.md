# LittleHorse Go Examples

Go SDK examples for LittleHorse workflow features.

## Prerequisites

- Running LittleHorse server (see [`../README.md`](../README.md)).
- `lhctl` installed and configured.
- Go toolchain installed.

Verify server connectivity:

```bash
lhctl whoami
```

## Running Go examples

Most Go examples have separate worker and deploy programs.

```bash
# Terminal 1 (keep running)
go run ./examples/go/basic/worker

# Terminal 2
go run ./examples/go/basic/deploy
lhctl run basic-workflow name Obi-Wan
```

## Go Example Index

- [`basic/`](./basic/README.md): Minimal hello-world workflow.
- [`bytes/`](./bytes/README.md): Byte-array input/output handling.
- [`checkpoint/`](./checkpoint/README.md): Checkpoint task usage.
- [`childthread/`](./childthread/README.md): Child-thread orchestration.
- `conditionals/`: Conditional examples (`ifelse`, `while`) source (no README yet).
- [`exceptionhandler/`](./exceptionhandler/README.md): Error handler patterns.
- [`externalevent/`](./externalevent/README.md): Wait for external events.
- [`interrupt/`](./interrupt/README.md): Interrupt handling.
- [`jsonarray/`](./jsonarray/README.md): JSON array variables.
- [`jsonobj/`](./jsonobj/README.md): JSON object variables.
- [`structdef/`](./structdef/README.md): Struct definition and usage.
- [`taskmetadata/`](./taskmetadata/README.md): Task metadata usage.
- [`wait-for-condition/`](./wait-for-condition/README.md): Wait-for-condition pattern.
