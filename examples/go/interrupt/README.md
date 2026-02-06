# Interrupt

This is a simple example of a workflow that uses an `ExternalEvent` to trigger an Interrupt Handler.

## Start the Task Worker

We have two `TaskDef`'s and thus two Task Functions. Note that `worker/main.go` kicks off two threads, one for each Task Worker.

```
go run ./examples/go/interrupt/worker
```

## Register the `WfSpec`

In another terminal, run:

```
go run ./examples/go/interrupt/deploy
```

## Run a `WfRun`

Let's run the `WfRun`:

```
lhctl run interrupt-example
```

Note the `wfRunId`. The `WfRun` will be sleeping for 120 seconds, in which time you can add to the tally by sending an interrupt event to it:

```
lhctl postEvent <wfRunId> update-tally INT 10
```

Then you can see all of the `ThreadRuns` in the `WfRun` by:

```
lhctl get wfRun <wfRunId>
```

And you can see the final tally via:

```
lhctl get variable <wfRunId> 0 tally
```
