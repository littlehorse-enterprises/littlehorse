# Rust casting example

Cast string inputs to a double and then an integer before typed task execution.

Adapted from the [python example](../../python/casting/example_casting.py).
Requires Rust 1.89+, `lhctl`, and LittleHorse v1.3.1 or current source.
See the [shared setup and configuration](../README.md).

## Run

From the repository root:

```sh
lhctl whoami
cargo run --manifest-path examples/rust/casting/Cargo.toml
```

Keep that terminal open; once registration is printed, use another terminal:

```sh
lhctl run rust-casting string-number 3.14
```

The double worker multiplies 3.14 by 0.9; the workflow casts that result to integer 2, and the integer worker doubles it to output `4`.

Inspect the result with `lhctl get wfRun <workflow-id>`. Press Ctrl-C in the
example terminal to drain workers and exit.
