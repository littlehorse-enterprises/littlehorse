## Running a Basic Example

This is a simple example, which does two things:

1. Declare an "input-name" variable of type String
2. Pass that variable into the execution of the "greet" task.

Let's run the example:

```
poetry shell
python -m example_basic
```

In another terminal, use `lhctl` to run the workflow:

```
# The "input-name" variable here is treated as null
lhctl run example-basic

# Here, we specify that the "input-name" variable = "Obi-Wan"
lhctl run example-basic input-name Obi-Wan

# This call fails since there is no defined "foo" variable
lhctl run example-basic foo bar
```

In addition, you can check the result with:

```
# This call shows the result
lhctl get wfRun <wf_run_id>

# This will show you all nodes in the run
lhctl list nodeRun <wf_run_id>

# This shows the task run information
lhctl get taskRun <wf_run_id> <task_run_global_id>
```

## Shutdown

`example_basic` uses the SDK's existing SIGTERM/SIGINT handlers. After a signal,
`littlehorse.start(worker)` returns only after accepted executions **and result
acknowledgements** finish. Its configured drain deadline is 30 seconds.

For optional application-owned POSIX signal handling:

```bash
python -m example_graceful_shutdown
```

This version installs application handlers that call synchronous `worker.stop()`
and runs `littlehorse.start(worker, handle_signals=False)`. It uses environment
configuration with no file handling. Repeated signals are safe and do not restart
its 20-second deadline.

Deadline expiry cancels/joins owned work and raises `TimeoutError`; exhausted
report retries raise `TaskResultReportingException`. Cancelling `start()` aborts
and propagates cancellation. These errors are deliberately not turned into a
successful exit. Task code must cooperate with cancellation.

For Kubernetes, allow **drain deadline + application cleanup + margin** before
SIGKILL. More termination grace alone does not establish result-report ownership
or exactly-once external effects. See the
[SDK shutdown documentation](../../../sdk-python/README.md#graceful-worker-shutdown)
for admission boundaries, retries, shared connections, cancellation, and limits.
