# Rust checkpoint-tasks example

Persist a checkpoint, fail the task, and replay its result on the retry.

Adapted from the [python example](../../python/checkpoint_tasks/example_checkpoint_tasks.py).
Requires Rust 1.89+, `lhctl`, and LittleHorse v1.3.1 or current source.
See the [shared setup and configuration](../README.md).

## Run

From the repository root:

```sh
lhctl whoami
cargo run --manifest-path examples/rust/checkpoint-tasks/Cargo.toml
```

Keep that terminal open; once registration is printed, use another terminal:

```sh
lhctl run rust-checkpoint-tasks
```

`Executing first checkpoint` prints once during the ordinary retry, while the handler runs twice. The output is `hello Ada, second checkpoint complete`.

Keep checkpoint order stable. Real side effects still need idempotency: a crash between a side effect and checkpoint persistence can repeat the side effect.

Inspect the result with `lhctl get wfRun <workflow-id>`. Press Ctrl-C in the
example terminal to drain workers and exit.
