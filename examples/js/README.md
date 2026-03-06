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

Example (`simple-worker`):

```bash
cd examples/js/simple-worker
npm install
npm start
```

Use another terminal for workflow registration/runs:

```bash
lhctl deploy wfSpec example-basic-wfspec.json
```

## JavaScript Example Index

- [`simple-worker/`](./simple-worker/README.md): Basic task worker example.
- [`structs/`](./structs/README.md): Struct-based worker and workflow example.
