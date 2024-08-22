# Child Threads

This is a simple example of a workflow that launches a Child Thread and waits for its completion.

## Start the Task Worker

We have two `TaskDef`'s and thus two Task Functions. Note that `worker/main.go` kicks off two threads, one for each Task Worker.

```
go run ./examples/childthread/worker
```

## Register the `WfSpec`

In another terminal, run:

```
go run ./examples/childthread/deploy
```

## Run a `WfRun`

Let's run the `WfRun`:

```
lhctl run child-thread-workflow input "this is the input!"
```

You can see both `ThreadRun`s when you inspect the `WfRun`:

```
lhctl get wfRun <wfRunId>
```
