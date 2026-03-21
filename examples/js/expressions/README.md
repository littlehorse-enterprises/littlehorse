# JavaScript Expressions Example

[`examples/java/expressions`](../../../java/expressions/README.md) — variable math into one task input.

## Prerequisites

- Node.js >= 18
- LittleHorse server on `localhost:2023` (see [`local-dev/README.md`](../../../local-dev/README.md))
- Build the SDK: `cd ../../../sdk-js && npm install && npm run build`

## Setup

```bash
npm install
```

## Run

```bash
npm start
```

Other terminal:

```bash
npm run run-wf -- 2 10 8
```

## Inspect

```bash
lhctl get wfRun <wf_run_id>
lhctl list nodeRun <wf_run_id>
```
