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
# Register the WfSpec (only needed once). You can also do this with the
# dashboard or programmatically using the gRPC client.
lhctl deploy wfSpec '{
  "name": "example-basic-js",
  "threadSpecs": {
    "entrypoint": {
      "variableDefs": [
        { "varDef": { "name": "input-name", "type": "STR" } }
      ],
      "nodes": {
        "0-entrypoint-ENTRYPOINT": { "entrypoint": {}, "outgoingEdges": [{ "sinkNodeName": "1-greet-TASK" }] },
        "1-greet-TASK": {
          "task": { "taskDefId": { "name": "greet" }, "variables": [{ "variableName": "input-name" }] },
          "outgoingEdges": [{ "sinkNodeName": "2-exit-EXIT" }]
        },
        "2-exit-EXIT": { "exit": {} }
      }
    }
  },
  "entrypointThreadName": "entrypoint"
}'
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
