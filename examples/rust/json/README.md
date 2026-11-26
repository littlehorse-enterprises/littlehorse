# Rust json example

Extract JSON paths and send their values to greeting and car-description tasks.

Adapted from the [python example](../../python/json/example_json.py).
Requires Rust 1.89+, `lhctl`, and LittleHorse v1.3.1 or current source.
See the [shared setup and configuration](../README.md).

## Run

From the repository root:

```sh
lhctl whoami
cargo run --manifest-path examples/rust/json/Cargo.toml
```

Keep that terminal open; once registration is printed, use another terminal:

```sh
lhctl run rust-json
```

The default JSON person is Ada, who drives a Toyota Camry. The final output is `You drive a Toyota Camry.`. Edit the default JSON or pass a `person` JSON input.

Inspect the result with `lhctl get wfRun <workflow-id>`. Press Ctrl-C in the
example terminal to drain workers and exit.
