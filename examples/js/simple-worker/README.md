# JavaScript Basic Example

A simple example that demonstrates using the LittleHorse JavaScript SDK to create a Task Worker.

The worker executes a `greet` task that takes a name and returns a greeting message.

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

This will:
1. Register the `greet` TaskDef if it doesn't exist
2. Start polling for tasks

## Run a Workflow

Before running a workflow, register the WfSpec using `lhctl`:

```bash
lhctl deploy wfSpec example-basic-wfspec.json
```
Then in another terminal:

```bash
# Run with a name
npm run run-wf -- Obi-Wan

# Run with default name
npm run run-wf
```

## Check Results

```bash
lhctl get wfRun <wf_run_id>
lhctl list nodeRun <wf_run_id>
lhctl get taskRun <wf_run_id> <task_run_global_id>
```
