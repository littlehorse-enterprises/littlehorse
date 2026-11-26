# Rust hierarchical-workflow example

Expose a public parent variable and read it from a separately started child workflow.

Adapted from the [python example](../../python/hierarchical_workflow/hierarchical_workflow.py).
Requires Rust 1.89+, `lhctl`, and LittleHorse v1.3.1 or current source.
See the [shared setup and configuration](../README.md).

## Run

From the repository root:

```sh
lhctl whoami
cargo run --manifest-path examples/rust/hierarchical-workflow/Cargo.toml
```

Keep that terminal open; once registration is printed, use another terminal:

```sh
lhctl run rust-hierarchical-workflow input-name Ada
lhctl run rust-hierarchical-child --parentWfRunId <parent-workflow-id>
lhctl postEvent <parent-workflow-id> rust-hierarchy-finished BOOL true
```

The child inherits `input-name` and returns `Hello, Ada!`. After the child completes, send the event to release the parent; it returns `Ada`.

Inspect the result with `lhctl get wfRun <workflow-id>`. Press Ctrl-C in the
example terminal to drain workers and exit.
