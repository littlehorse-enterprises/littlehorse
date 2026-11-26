# Rust interrupt-handler example

Handle an event in an interrupt thread while the main thread is waiting.

Adapted from the [python example](../../python/interruption/example_interruption.py).
Requires Rust 1.89+, `lhctl`, and LittleHorse v1.3.1 or current source.
See the [shared setup and configuration](../README.md).

## Run

From the repository root:

```sh
lhctl whoami
cargo run --manifest-path examples/rust/interrupt-handler/Cargo.toml
```

Keep that terminal open; once registration is printed, use another terminal:

```sh
lhctl run rust-interrupt-handler
lhctl postEvent <workflow-id> rust-interrupt STR hello
lhctl postEvent <workflow-id> rust-interrupt-finish BOOL true
```

The interrupt runs a task and increments the shared count. The main thread waits until that handler has finished, then consumes the finish event and returns `1`.

Inspect the result with `lhctl get wfRun <workflow-id>`. Press Ctrl-C in the
example terminal to drain workers and exit.
