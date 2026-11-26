# Rust run-child-workflow example

Register a separate child WfSpec, invoke it, and return its result.

Adapted from the [python example](../../python/run_child_wf/example_run_child_wf.py).
Requires Rust 1.89+, `lhctl`, and LittleHorse v1.3.1 or current source.
See the [shared setup and configuration](../README.md).

## Run

From the repository root:

```sh
lhctl whoami
cargo run --manifest-path examples/rust/run-child-workflow/Cargo.toml
```

Keep that terminal open; once registration is printed, use another terminal:

```sh
lhctl run rust-run-child-workflow
```

The child greets Ada; the parent waits for it and returns `Hello from child workflow, Ada!`.

Inspect the result with `lhctl get wfRun <workflow-id>`. Press Ctrl-C in the
example terminal to drain workers and exit.
