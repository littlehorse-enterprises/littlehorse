# Rust conditionals example

Choose a donut or salad with a workflow if/else.

Adapted from the [go example](../../go/conditionals/ifelse/workflow.go).
Requires Rust 1.89+, `lhctl`, and LittleHorse v1.3.1 or current source.
See the [shared setup and configuration](../README.md).

## Run

From the repository root:

```sh
lhctl whoami
cargo run --manifest-path examples/rust/conditionals/Cargo.toml
```

Keep that terminal open; once registration is printed, use another terminal:

```sh
lhctl run rust-conditionals number-of-donuts 3
lhctl run rust-conditionals number-of-donuts 12
```

The output is `Have another donut!` below 10 donuts and `Have a salad!` otherwise.

Inspect the result with `lhctl get wfRun <workflow-id>`. Press Ctrl-C in the
example terminal to drain workers and exit.
