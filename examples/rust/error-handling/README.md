# Rust error-handling example

Handle technical task errors and named business exceptions separately.

Adapted from the [python example](../../python/error_handling/example_error_handling.py).
Requires Rust 1.89+, `lhctl`, and LittleHorse v1.3.1 or current source.
See the [shared setup and configuration](../README.md).

## Run

From the repository root:

```sh
lhctl whoami
cargo run --manifest-path examples/rust/error-handling/Cargo.toml
```

Keep that terminal open; once registration is printed, use another terminal:

```sh
lhctl run rust-error-handling kind technical
lhctl run rust-error-handling kind business
```

The workflow completes with `technical error handled` or `business exception handled`.

Inspect the result with `lhctl get wfRun <workflow-id>`. Press Ctrl-C in the
example terminal to drain workers and exit.
