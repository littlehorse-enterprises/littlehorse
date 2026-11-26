# LittleHorse Python SDK

For documentation on how to use this library, please go to [the LittleHorse website](https://littlehorse.io).

For examples go to the [examples](./examples/) folder.

## Dependencies

- Install Python 3.11–3.15.
- Install [pipx](https://github.com/pypa/pipx): `brew install pipx`
- Install [poetry](https://python-poetry.org/): `pipx install poetry`
- Install [poetry shell plugin](https://github.com/python-poetry/poetry-plugin-shell): `poetry self add poetry-plugin-shell`

## Initialize

```
poetry install
```

## Protobuf Compilation

```
../local-dev/compile-proto.sh
```

## Run tests

```
poetry shell
python -m unittest discover -v
```

## Validate Indentations
```
poetry run ruff check .
```

## Validate types
```
poetry run mypy .
```

## Useful Commands

Set python version:

```
poetry env use python3.11
```

## Graceful worker shutdown

The existing integration handles `SIGTERM` and `SIGINT` automatically:

```python
worker = LHTaskWorker(greeting, "greet", config, shutdown_timeout=75.0)
await littlehorse.start(worker)
# Accepted executions AND their result reports have finished successfully.
# Application resources used by tasks can now be closed.
```

`worker.stop()` is a synchronous shutdown **request**, not a completion signal.
The running `worker.start()` or `littlehorse.start(...)` coroutine is the
completion boundary: keep awaiting it until it returns or raises. Calling
`stop()` repeatedly is safe, including before startup; it does not restart the
deadline or cancel accepted work. A stopped worker cannot be restarted.

On the first stop request the SDK stops heartbeat registration and polling.
Every TaskRun already received by the polling consumer while admission was open
is owned by the SDK, including tasks waiting for execution capacity. Shutdown
waits for those executions and their result acknowledgements, including reporting
retries. Reporting does not occupy execution slots. Connections removed/replaced
during heartbeat reconciliation retain ownership of their accepted work.
Replies not delivered to the consumer before admission closes are outside this
boundary; tasks claimed by the server but not received may need server-side
timeout/retry recovery.

### Deadlines, failures, and cancellation

`shutdown_timeout` is a positive finite number of seconds, default **75**,
measured from the first `stop()` request, not from startup. Each worker has its
own deadline; `littlehorse.start(*workers)` waits for all of them. This allows the
default 60-second task timeout plus 15 seconds for reporting, not a guarantee that
every reporting retry can finish within the deadline.

The first shutdown request logs the timeout and outstanding accepted-task count
at INFO level, including queued executions and pending reports on retired
connections.

- Successful return means accepted task executions finished and their results
  were acknowledged. A task's ordinary business/technical failure is still
  serialized and reported as usual; it does not itself make draining fail.
- Deadline expiry cancels and joins SDK-owned polling, execution, and reporting
  tasks, then raises built-in `TimeoutError`. This is **not** graceful success.
- Exhausting normal result-reporting retries raises
  `littlehorse.exceptions.TaskResultReportingException`, after draining other
  accepted work (or reaching the deadline). A reporting failure that occurred
  before shutdown is retained and cannot later become graceful success.
- Cancelling `start()` aborts and joins owned work and propagates
  `asyncio.CancelledError`. It is not equivalent to calling `stop()`.
  Unrelated application asyncio tasks are not cancelled.
- Cancellation cleanup is cooperative and may extend beyond the drain deadline.
  Task code must yield to the event loop and propagate cancellation after its
  cleanup. Blocking code or code suppressing cancellation cannot be forcibly
  stopped by asyncio.

Keep application-owned resources and the `LHConfig`/RPC channels usable until
`start()` finishes. After an exception, owned work has been joined, so application
cleanup can run, but the application must treat shutdown as unsuccessful.
Shared configuration channels are not closed by worker draining.

### Application-owned signals (optional)

Most applications can keep the SDK's default handlers. If the application needs
to own signals, pass `handle_signals=False`; the SDK will not install or replace
any signal handlers. Register handlers that call the same public `worker.stop()`
method, and still await `start()`:

```python
import asyncio
import io
import signal

import littlehorse
from littlehorse.config import LHConfig
from littlehorse.worker import LHTaskWorker


async def main() -> None:
    config = LHConfig()
    with io.StringIO() as application_resource:
        async def record(message: str) -> str:
            application_resource.write(message + "\n")
            return message

        littlehorse.create_task_def(record, "record", config)
        worker = LHTaskWorker(record, "record", config, shutdown_timeout=20.0)
        loop = asyncio.get_running_loop()
        for sig in (signal.SIGTERM, signal.SIGINT):
            loop.add_signal_handler(sig, worker.stop)
        try:
            await littlehorse.start(worker, handle_signals=False)
        finally:
            for sig in (signal.SIGTERM, signal.SIGINT):
                loop.remove_signal_handler(sig)
    # The application resource closes only after SDK draining/cancellation cleanup.
    # Shutdown exceptions propagate; they are not converted to success.


if __name__ == "__main__":
    asyncio.run(main())
```

This application-owned example uses POSIX asyncio signal handlers. The default
SDK `shutdown_hook(*workers)` also supports runtimes without asyncio signal
handlers (for example, Windows) by falling back to `signal.signal(...)`.
If registration is unavailable in the runtime/thread, startup continues; request
shutdown with `worker.stop()` from the running event loop and await `start()`.

See the [basic example](../examples/python/basic/README.md) for both styles.

### Kubernetes and delivery limits

Budget `terminationGracePeriodSeconds` for **drain deadline + application cleanup
+ margin** (use the largest concurrent worker deadline). A 75-second drain
deadline needs more than a 75-second termination grace period. Cancellation
cleanup also consumes the budget.

Increasing termination grace alone does not fix missing reporting ownership;
waiting only for task functions or an application execution counter does not
cover result acknowledgement. SIGKILL, process failure, or permanent network
failure can still prevent completion. A result acknowledgement does not promise
exactly-once external side effects: retain appropriate task idempotency and
workflow timeout/retry policies.

## Checkpoint callbacks

`await ctx.execute_and_checkpoint(callback)` supports synchronous callbacks and
asynchronous callbacks, including synchronous wrappers returning awaitables. Each
callback receives a `CheckpointContext`. The SDK awaits an awaitable result, then
persists the completed serializable value and the callback's checkpoint logs before
returning the result.

For example, inside an async task using an async HTTP client:

```python
from littlehorse.worker import CheckpointContext, WorkerContext


async def fetch_result(url: str, ctx: WorkerContext) -> dict:
    async def fetch(checkpoint_ctx: CheckpointContext) -> dict:
        response = await http_client.get(url)  # Your application's async HTTP client.
        response.raise_for_status()
        checkpoint_ctx.log("Request completed")
        return response.json()

    return await ctx.execute_and_checkpoint(fetch)
```

On replay, calls return previously saved checkpoint values in order without
invoking their callbacks or repeating their I/O. Keep checkpoint calls in the same
order across retries. Callback exceptions and cancellation propagate without saving
an incomplete result. This does not guarantee exactly-once execution: work may run
again if it completes but the checkpoint is not persisted. Synchronous callbacks
still execute on the worker event loop, so use async clients for nonblocking I/O.

## Types Map

Task arguments type reference:

```
VariableType.JSON_OBJ: dict[str, Any]
VariableType.JSON_ARR: list[Any]
VariableType.DOUBLE:   float
VariableType.BOOL:     bool
VariableType.STR:      str
VariableType.INT:      int
VariableType.BYTES:    bytes
```

## Python Code Formatter 

```
poetry shell
black . 
```
