# Rust correlated-event example

Correlate an external callback using a business key instead of a workflow ID.

Adapted from the [python example](../../python/correlated_event/example_correlated_event.py).
Requires Rust 1.89+, `lhctl`, and LittleHorse v1.3.1 or current source.
See the [shared setup and configuration](../README.md).

## Run

From the repository root:

```sh
lhctl whoami
cargo run --manifest-path examples/rust/correlated-event/Cargo.toml
```

It registers the metadata and exits; the workflow runs on the server. Then:

```sh
lhctl run rust-correlated-event order-id order-123
lhctl put correlatedEvent order-123 rust-order-paid BOOL true
```

The workflow completes with the callback boolean. Use a different order ID for independent concurrent runs; correlation consumes the event once.

Inspect the result with `lhctl get wfRun <workflow-id>`.
