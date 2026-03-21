# JavaScript Basic Example

## Prerequisites

- Node.js >= 18
- A running LittleHorse server on `localhost:2023` (see [local-dev/README.md](../../local-dev/README.md))
- Build the SDK first: `cd ../../sdk-js && npm install && npm run build`

## Setup

```bash
npm install
```

## Run the Task Worker

```bash
npm start
```

Registers TaskDef + WfSpec, then polls.

## Run a Workflow

In another terminal:

```bash
# Run with a name
npm run run-wf -- Obi-Wan

# Run with default name
npm run run-wf
```

Or with `lhctl`:

```bash
lhctl run example-basic input-name Obi-Wan
```

## Check Results

```bash
lhctl get wfRun <wf_run_id>
lhctl list nodeRun <wf_run_id>
lhctl get taskRun <wf_run_id> <task_run_global_id>
```
