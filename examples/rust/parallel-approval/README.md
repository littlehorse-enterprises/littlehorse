# Rust parallel-approval example

Fan out one human approval thread per reviewer, then join all threads.

Adapted from the [python example](../../python/parallel_approvals/parallel_approvals.py).
Requires Rust 1.89+, `lhctl`, and LittleHorse v1.3.1 or current source.
See the [shared setup and configuration](../README.md).

## Run

From the repository root:

```sh
lhctl whoami
cargo run --manifest-path examples/rust/parallel-approval/Cargo.toml
```

It registers the metadata and exits; the workflow runs on the server. Then:

```sh
lhctl run rust-parallel-approval
lhctl list userTaskRun <workflow-id>
```

Two tasks are assigned to alice and bob. Complete each using its GUID from the list:

```sh
lhctl execute userTaskRun <workflow-id> <user-task-guid>
```

Enter the assigned user ID (`alice` or `bob`), then `true` or `false` for `approved`.
Repeat for the other task. Output is `true` only if both approve.

The form uses primitive boolean fields for v1.3.1 compatibility.

Inspect the result with `lhctl get wfRun <workflow-id>`.
