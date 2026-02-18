# External Event

This is a simple example of a workflow that waits for an `ExternalEvent`.

## Start the Task Worker

We have two `TaskDef`'s and thus two Task Functions. Note that `worker/main.go` kicks off two threads, one for each Task Worker.

```
go run ./examples/go/externalevent/worker
```

## Register the `WfSpec`

In another terminal, run:

```
go run ./examples/go/externalevent/deploy
```

## Run a `WfRun`

Let's run the `WfRun`:

```
lhctl run external-event
```

Note the `wfRunId`. Let's check on it:

```
lhctl get wfRun <wfRunId>
```

The status should be `RUNNING`. That's because it's waiting for us to send the event.

```
lhctl postEvent <wfRunId> my-name STR obi-wan
```

That sends the `ExternalEvent` to the `WfRun`. We can view the status and see that the workflow completed:

```
lhctl get wfRun <wfRunId>
```
