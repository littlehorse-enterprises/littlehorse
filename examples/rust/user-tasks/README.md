# Rust user-tasks example

Assign a human approval to alice and return the submitted boolean.

Adapted from the [python example](../../python/user_tasks/user_tasks.py).
Requires Rust 1.89+, `lhctl`, and LittleHorse v1.3.1 or current source.
See the [shared setup and configuration](../README.md).

## Run

From the repository root:

```sh
lhctl whoami
cargo run --manifest-path examples/rust/user-tasks/Cargo.toml
```

It registers the metadata and exits; the workflow runs on the server. Then:

```sh
lhctl run rust-user-tasks
lhctl list userTaskRun <workflow-id>
```

Use the returned user-task GUID to complete alice's task:

```sh
lhctl execute userTaskRun <workflow-id> <user-task-guid>
```

Enter `alice` when prompted for the user ID, then `true` or `false` for `approved`.
The workflow returns that boolean.

This example uses the legacy primitive form supported by v1.3.1 and current source; see the SDK README for struct-backed forms.

Inspect the result with `lhctl get wfRun <workflow-id>`.
