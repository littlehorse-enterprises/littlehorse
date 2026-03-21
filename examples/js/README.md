# LittleHorse JavaScript Examples

JavaScript/TypeScript SDK examples for LittleHorse workers.

## Prerequisites

- Running LittleHorse server (see [`../README.md`](../README.md)).
- `lhctl` installed and configured.
- Node.js (see per-example README for version details).

Verify server connectivity:

```bash
lhctl whoami
```

## Running JS examples

Example (`basic`):

```bash
cd examples/js/basic
npm install
npm start
```

Use another terminal to start a run (see each example’s README).

## JavaScript Example Index

| JS folder | Java equivalent |
|-----------|------------------|
| [`basic/`](./basic/README.md) | `examples/java/basic` |
| [`expressions/`](./expressions/README.md) | `examples/java/expressions` |
| [`json/`](./json/README.md) | `examples/java/json` |
| [`mutation/`](./mutation/README.md) | `examples/java/mutation` |
| [`structs/`](./structs/README.md) | `examples/java/struct-def` |
| [`variables/`](./variables/README.md) | `examples/java/variables` |
| [`worker-context/`](./worker-context/README.md) | `examples/java/worker-context` |

### Not in the minimal JS workflow builder yet

`casting`, `conditionals`, `external-event`, `child-thread`, `user-tasks`, `exception-handler`, `checkpoint-tasks`, …
