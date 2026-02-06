## Running Checkpointed Task Examples

This is a simple example which shows the usage of checkpoints in tasks.

## How Checkpoints Work

This example demonstrates checkpoint functionality with a task that executes a checkpointable function, checkpoints the result of that function, and then on subsequent task attempts retrieves the checkpointed result instead of re-executing the function. This prevents you from re-executing expensive deterministic processes, adding durable execution functionality to your tasks.

## What's Going On

The file [workflow.go](./workflow.go) has two functions defined:

- `GreetWithCheckpoints`, which is a Task Function.
- `MyWorkflow`, which is the Workflow Function.

## Deploy the Task Worker

Before we can create the `WfSpec`, we need to register the `TaskDef`. The easiest way to do that is to run the task worker:

```
go run ./examples/go/checkpoint/worker
```

Leave that process running.

## Register the `WfSpec`

Next, in another terminal, run:

```
go run ./examples/go/checkpoint/deploy
```

That will create the `WfSpec`. You can verify that via:

```
lhctl get wfSpec example-checkpointed-tasks
```

## Run a `WfRun`

To run a `WfRun`, you can use `lhctl`.

```
lhctl run example-checkpointed-tasks input-name obi-wan
```

It will print out an ID. You can view the status of that `WfRun` via:

```
lhctl get wfRun <the id from the previous step>
```

## Worker Output

Watch the worker output to see how the second execution skips the first checkpoint and directly retrieves its saved result.

