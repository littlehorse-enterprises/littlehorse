# Exception Handler

This is a simple example of a workflow that uses a `FailureHandler` to recover from a flaky task.

## Start the Task Worker

We have two `TaskDef`'s and thus two Task Functions. Note that `worker/main.go` kicks off two threads, one for each Task Worker.

```
go run ./examples/go/exceptionhandler/worker
```

## Register the `WfSpec`

In another terminal, run:

```
go run ./examples/go/exceptionhandler/deploy
```

## Run a `WfRun`

Let's run the `WfRun`:

```
lhctl run exception-handler
```

Then you can see all of the `ThreadRuns` in the `WfRun` by:

```
lhctl get wfRun <wfRunId>
```
