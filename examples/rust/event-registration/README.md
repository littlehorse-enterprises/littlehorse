# Rust event-registration example

Register inbound event types and an outbound workflow event before deploying a summary workflow.

Adapted from the [python example](../../python/event_registration/example_event_registration.py).
Requires Rust 1.89+, `lhctl`, and LittleHorse v1.3.1 or current source.
See the [shared setup and configuration](../README.md).

## Run

From the repository root:

```sh
lhctl whoami
cargo run --manifest-path examples/rust/event-registration/Cargo.toml
```

It registers the metadata and exits; the workflow runs on the server. Then:

```sh
lhctl run rust-event-registration
lhctl postEvent <workflow-id> rust-summary-name STR Ada
lhctl postEvent <workflow-id> rust-summary-age INT 37
```

The workflow waits for a name and age, emits `rust-summary-ready`, and returns `Summary: Ada, age 37`. Inspect emitted events with `lhctl list workflowEvent <workflow-id>`.

Inspect the result with `lhctl get wfRun <workflow-id>`.
