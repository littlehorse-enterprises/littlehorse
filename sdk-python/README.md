# LittleHorse Python SDK

For documentation on how to use this library, please go to [the LittleHorse website](https://littlehorse.io).

For examples go to the [examples](./examples/) folder.

## Dependencies

- Install python.
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
poetry env use python3.9
```

## Worker shutdown on Windows

`littlehorse.start(...)` installs shutdown hooks for `SIGTERM` and `SIGINT`.
On runtimes that do not support `asyncio` signal handlers (for example, Windows
event loops), the SDK falls back to `signal.signal(...)` when available. If a
signal cannot be registered in the current runtime/thread, startup still
continues and workers can be stopped by calling `worker.stop()`.

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
