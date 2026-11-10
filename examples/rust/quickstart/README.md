# Rust quickstart

This example runs the same know-your-customer workflow as the Java, Go, Python,
C#, and JavaScript quickstarts. It registers `verify-identity`,
`notify-customer-verified`, `notify-customer-not-verified`, the correlated
`identity-verified` event, and the `quickstart` workflow, then starts three workers.
The verification task simulates submitting to an identity service; you send its
callback with `lhctl`.

Requires Rust 1.89 or later, Docker, and [lhctl](../../../README.md#install-the-littlehorse-cli).
Use synthetic customer data for this example.

## Run

From the repository root, start LittleHorse and its dashboard:

```sh
docker run --rm --pull=always --name littlehorse -d \
  -p 2023:2023 -p 8080:8080 \
  ghcr.io/littlehorse-enterprises/littlehorse/lh-standalone:latest
lhctl whoami
cargo run --manifest-path examples/rust/quickstart/Cargo.toml
```

Wait for `Quickstart registered. Task workers running`, then in another terminal:

```sh
lhctl run quickstart full-name 'Obi-Wan Kenobi' email obiwan@jedi.temple ssn 123456789
```

Save the returned workflow ID. Open <http://localhost:8080> to inspect the run;
it waits for identity verification. Submit the approval callback:

```sh
lhctl put correlatedEvent obiwan@jedi.temple identity-verified BOOL true
```

The worker prints `[notify-customer-verified] Identity confirmed ...` and the run
reaches `COMPLETED`. Inspect it with `lhctl get wfRun <workflow-id>` or the dashboard.
Start another run with a different email and send `BOOL false` to that email to
exercise `notify-customer-not-verified`. The rejection path also completes normally.
If no callback arrives within five minutes, the timeout handler sends a rejection
notification and fails the workflow.

Press Ctrl-C to drain and stop the workers. Stop the container with
`docker stop littlehorse`. Avoid running another language's quickstart concurrently:
they share workflow and task names.

## Configuration and tests

The example reads `~/.config/littlehorse.config` when present. `LHC_` environment
variables override file settings; defaults connect to localhost:2023. See the
[SDK README](../../../sdk-rust/README.md) for connection and authentication settings.

With a running server, this test executes both approval and rejection paths and
checks which notification task ran:

```sh
cargo test --manifest-path examples/rust/quickstart/Cargo.toml --locked -- --ignored --nocapture
```

GitHub CI runs this test against v1.3.1 and the server built from the current source.
