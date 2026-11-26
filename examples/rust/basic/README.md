# Rust basic example

A minimal greeting workflow using `Workflow::define`, a typed `String` task
worker, and cluster polling. Start here to learn the workflow DSL. For the KYC
identity-verification scenario, see [quickstart](../quickstart/README.md).

Requires Rust 1.89 or later, `lhctl`, and a running LittleHorse server (v1.3.1 or
the current source). See the [local server setup](../../README.md).

## Run

From the repository root:

```sh
lhctl whoami
cargo run --manifest-path examples/rust/basic/Cargo.toml
```

Keep the worker running. When it prints `Registered rust-basic`, run this in
another terminal:

```sh
lhctl run rust-basic name Ada
```

The worker prints `Hello, Ada!`. Inspect the returned workflow ID:

```sh
lhctl get wfRun <workflow-id>
```

The run reaches `COMPLETED`, and its entrypoint thread output is `Hello, Ada!`.
Running `lhctl run rust-basic` without inputs uses the default name `world`.
Press Ctrl-C in the worker terminal to drain accepted work and stop.

The example reads `~/.config/littlehorse.config` when present. `LHC_` environment
variables override file settings; defaults connect to localhost:2023. See the
[SDK configuration reference](../../../sdk-rust/README.md#clients-and-authentication).
