# Rust retries example

Retry a technical task failure using the per-run attempt number.

Adapted from the [python example](../../python/retries/retries.py).
Requires Rust 1.89+, `lhctl`, and LittleHorse v1.3.1 or current source.
See the [shared setup and configuration](../README.md).

## Run

From the repository root:

```sh
lhctl whoami
cargo run --manifest-path examples/rust/retries/Cargo.toml
```

Keep that terminal open; once registration is printed, use another terminal:

```sh
lhctl run rust-retries
```

Attempt 0 deliberately fails; attempt 1 succeeds with `Succeeded on attempt 1`. The failure is deterministic and scoped to each TaskRun.

Inspect the result with `lhctl get wfRun <workflow-id>`. Press Ctrl-C in the
example terminal to drain workers and exit.
