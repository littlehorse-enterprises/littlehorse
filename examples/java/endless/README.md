## Running EndlessExample

This example registers the `example-endless` `WfSpec`, whose `WfRun`s stay `RUNNING` forever by default. It's handy for testing anything that deals with long-lived `WfRun`s (e.g. the live-updating Duration column in the dashboard).

Each loop iteration:

1. Executes the `endless-heartbeat` task, passing the current `iteration`.
2. Sleeps for `interval-seconds` (default `60`).

The loop exits only once `iteration` equals `max-iterations`. That defaults to `-1`, so the loop never exits.

Register the `WfSpec` and start the task worker (keep this running):

```
./gradlew example-endless:run
```

If you're using the `lh-standalone` docker image, point the example at its port with an env var:

```
LHC_API_PORT=2023 ./gradlew example-endless:run
```

In another terminal, use `lhctl` to run the workflow:

```
# Runs forever, with a heartbeat every 60 seconds
lhctl run example-endless

# Runs forever, with a heartbeat every 5 seconds
lhctl run example-endless interval-seconds 5

# Runs 3 iterations, 10 seconds apart, then COMPLETES (~30 seconds total)
lhctl run example-endless interval-seconds 10 max-iterations 3
```

An endless `WfRun` only ends when you stop it:

```
# Halts the WfRun (status goes to HALTED)
lhctl stop wfRun <wf_run_id>

# Deletes it
lhctl delete wfRun <wf_run_id>
```

You can inspect it with:

```
lhctl get wfRun <wf_run_id>
lhctl list nodeRun <wf_run_id>
```
