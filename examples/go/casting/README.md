# Casting Example (Go)

This example demonstrates both automatic and manual type casting in LittleHorse workflows.

Overview

The casting example shows how LittleHorse handles type conversions between different variable types.

Automatic Casting (no cast call required):
- `INT` → `DOUBLE`, `INT` → `STR`, `DOUBLE` → `STR`, `BOOL` → `STR` (common automatic conversions)

Manual Casting (requires `CastTo` calls):
- `STR` → `DOUBLE`
- `DOUBLE` → `INT`
- `STR` → `BOOL`
- JSON path result → `INT` (when type is ambiguous)

Files

- `workflow.go`: the workflow definition (`MyWorkflow`) that demonstrates casting patterns.
- `worker/main.go` + `worker/worker.go`: task worker that registers the task defs and runs handlers.
- `deploy/main.go`: registers the `WfSpec` with the LH server.

Run the example

1) Start a Task Worker (in one terminal):
```bash
cd examples/go/casting/worker
go run .
```

2) Register the workflow spec (in another terminal):
```bash
cd examples/go/casting/deploy
go run .
```

3) Start a workflow run (use `lhctl`):
```bash
lhctl run casting-workflow
```

Trigger the error handler

To make the workflow enter the error handler, run the workflow with an input that causes the server to fail when casting `hello` to a boolean. This will cause the node at thread 0 position 4 to fail and the handler thread to run; after the handler completes the workflow continues.

```bash
lhctl run casting-workflow string-bool Hello --wfRunId error-handler
```

To inspect the failed node run:

```bash
lhctl get nodeRun error-handler 0 4
```


