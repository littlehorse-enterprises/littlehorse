# Rust external-event example

Wait for an external event addressed to a specific workflow run.

Adapted from the [python example](../../python/external_event/example_external_event.py).
Requires Rust 1.89+, `lhctl`, and LittleHorse v1.3.1 or current source.
See the [shared setup and configuration](../README.md).

## Run

From the repository root:

```sh
lhctl whoami
cargo run --manifest-path examples/rust/external-event/Cargo.toml
```

Keep that terminal open; once registration is printed, use another terminal:

```sh
lhctl run rust-external-event
lhctl postEvent <workflow-id> rust-name-received STR Ada
```

After the event, the greeting worker returns `Hello, Ada!`. The wait times out after 60 seconds if no event arrives.

Inspect the result with `lhctl get wfRun <workflow-id>`. Press Ctrl-C in the
example terminal to drain workers and exit.
