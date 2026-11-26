# Rust mutation example

Assign a task result back to a workflow variable and pass it to another task.

Adapted from the [python example](../../python/mutation/example_mutation.py).
Requires Rust 1.89+, `lhctl`, and LittleHorse v1.3.1 or current source.
See the [shared setup and configuration](../README.md).

## Run

From the repository root:

```sh
lhctl whoami
cargo run --manifest-path examples/rust/mutation/Cargo.toml
```

Keep that terminal open; once registration is printed, use another terminal:

```sh
lhctl run rust-mutation name Peter
lhctl run rust-mutation name Ada
```

Peter, Miles, or Gwen transforms into Spider-Man; another name remains unchanged. The transformed message is the workflow output.

Inspect the result with `lhctl get wfRun <workflow-id>`. Press Ctrl-C in the
example terminal to drain workers and exit.
