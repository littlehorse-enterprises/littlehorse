# Rust struct-def example

Derive nested native StructDefs, look up a vehicle owner, and pass the typed result to a second worker.

Adapted from the [python example](../../python/structdef/example_structdef.py).
Requires Rust 1.89+, `lhctl`, and LittleHorse v1.3.1 or current source.
See the [shared setup and configuration](../README.md).

## Run

From the repository root:

```sh
lhctl whoami
cargo run --manifest-path examples/rust/struct-def/Cargo.toml
```

Keep that terminal open; once registration is printed, use another terminal:

```sh
lhctl run rust-struct-def
```

A sample parking report is passed to a typed worker, which returns a Person with
a nested Address. The mailing worker returns `Ticket sent to Obi-Wan Kenobi in
Anchorhead`. The Rust `license_plate` field uses `#[lh(rename = "licensePlate")]`
to match the server's field naming rules.

The nested address is unmasked for compatibility with v1.3.1 task-output
validation. All values are synthetic example data.

Inspect the result with `lhctl get wfRun <workflow-id>`. Press Ctrl-C in the
example terminal to drain workers and exit.
