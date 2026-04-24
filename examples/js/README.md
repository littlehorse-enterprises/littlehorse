# LittleHorse JavaScript Examples

JavaScript/TypeScript examples for LittleHorse workflow features.

## Prerequisites

- Running LittleHorse server (see [`../README.md`](../README.md)).
- `lhctl` installed and configured.
- Node.js 18+.
- npm (run `npm install` inside each example directory).

Verify server connectivity:

```bash
lhctl whoami
```

## Running JavaScript examples

From an example directory (after `npm install`):

```bash
npm start
```

Most examples start a long-lived worker process. Keep that command running and use another terminal for `npm run run-wf` or `lhctl run ...` (see each example’s README).

## JavaScript Example Index

- [`basic/`](./basic/README.md): Minimal hello-world workflow.
- [`expressions/`](./expressions/README.md): Expressions and variable manipulation.
- [`json/`](./json/README.md): JSON variable examples.
- [`mutation/`](./mutation/README.md): Variable mutation behavior.
- [`structs/`](./structs/README.md): Struct definition and usage.
- [`variables/`](./variables/README.md): Variable declaration and assignment.
- [`worker-context/`](./worker-context/README.md): Access worker context metadata.
