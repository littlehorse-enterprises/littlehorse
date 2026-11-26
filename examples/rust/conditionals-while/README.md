# Rust conditionals-while example

Eat donuts in a workflow loop until the counter reaches zero.

Adapted from the [python example](../../python/conditionals_while/example_conditionals_while.py).
Requires Rust 1.89+, `lhctl`, and LittleHorse v1.3.1 or current source.
See the [shared setup and configuration](../README.md).

## Run

From the repository root:

```sh
lhctl whoami
cargo run --manifest-path examples/rust/conditionals-while/Cargo.toml
```

Keep that terminal open; once registration is printed, use another terminal:

```sh
lhctl run rust-conditionals-while number-of-donuts 3
```

The worker prints 2, 1, then 0 donuts remaining. The workflow output is `0`.

Inspect the result with `lhctl get wfRun <workflow-id>`. Press Ctrl-C in the
example terminal to drain workers and exit.
