# Rust wait-for-condition example

Wait for a shared counter to reach zero; interrupt events decrement it.

Adapted from the [python example](../../python/wait_for_condition/wait_for_condition.py).
Requires Rust 1.89+, `lhctl`, and LittleHorse v1.3.1 or current source.
See the [shared setup and configuration](../README.md).

## Run

From the repository root:

```sh
lhctl whoami
cargo run --manifest-path examples/rust/wait-for-condition/Cargo.toml
```

It registers the metadata and exits; the workflow runs on the server. Then:

```sh
lhctl run rust-wait-for-condition
lhctl postEvent <workflow-id> rust-subtract BOOL true
lhctl postEvent <workflow-id> rust-subtract BOOL true
```

The run stays active after the first event and completes with output `0` after the second.

Inspect the result with `lhctl get wfRun <workflow-id>`.
