# Rust email-processing example

A scheduled pipeline: every five minutes, check the inbox and start one workflow run
per new email. Each run reads the email, analyzes it with an AI model, uploads the
result to a third-party service, and marks the email processed.

This is a template, not a working integration. The task logic and the workflow are
complete, but the integrations at the bottom of [`src/main.rs`](src/main.rs) are
`todo!()` stubs: listing and reading emails, the AI call, the upload, and moving the
email. Fill those in before running it against real data. Until then, each run fails
at its first stub, and the panic is reported as that task's failure.

Requires Rust 1.89+, `lhctl`, and LittleHorse v1.3.1 or current source.
See the [shared setup and configuration](../README.md).

## Structure

Two workflows:

- **`rust-check-inbox`** runs on a schedule (`*/5 * * * *`). Its one task lists new
  emails and starts a `rust-process-email` run for each, using the email's Message-ID
  as the run ID. An email seen by two polls already has a run, so it is skipped, and
  each email is processed once.
- **`rust-process-email`** handles one email:

  ```
  fetch-email → analyze-email → [confidence < 80: human review] → upload-analysis → mark-email-processed
  ```

Each email gets its own run, so you can find it by Message-ID, inspect it, and rerun
it without affecting the others.

The tasks are plain async functions with `#[task("name")]`, which generates each
worker and its TaskDef. Parameter names become the TaskDef's input names, and
registering a task also registers the `Email` and `EmailAnalysis` StructDefs it uses.

## Retries and failures

Every step chains its own options. Steps without options use the workflow default
from `default_task_options`: a 30-second timeout and 3 retries with 1s to 30s backoff.

| Step | Timeout | Retries | Backoff | When it gives up |
| --- | --- | --- | --- | --- |
| `start-email-processing` | 60s | 2 | 1s to 10s | The next scheduled poll tries again |
| `fetch-email` | default | default | default | The run ends in `ERROR`; rerun it |
| `analyze-email` | 120s | 5 | 2s to 60s | Business exception: dead-letter, then fail |
| `upload-analysis` | 60s | 5 | 5s to 5min | Any error: dead-letter, then fail |
| `mark-email-processed` | default | 10 | 1s to 60s | The run ends in `ERROR`; rerun it |

LittleHorse retries only technical failures: timeouts and `TaskFailure::Error`.
A `TaskFailure::Exception` is a business result, such as an email the AI model
refuses, and is never retried. Each stub's comments say which one to return.

Retries can repeat side effects, so:

- The upload sends the Message-ID as its idempotency key. If a retry repeats an
  upload that already succeeded, the third-party service can ignore the duplicate.
- The email is marked processed only after the upload succeeds, so a failure never
  loses an email.
- The AI worker runs at most four calls at a time (`concurrency = 4`), so a burst of
  emails doesn't overwhelm a rate-limited API.

Results with confidence below 80 wait for someone in the `email-reviewers` group to
approve them in the `rust-review-email-analysis` user task. A rejected result fails
the run with `rejected-by-reviewer`.

## Run

From the repository root:

```sh
lhctl whoami
cargo run --manifest-path examples/rust/email-processing/Cargo.toml
```

This registers the tasks, user task, workflows, and schedule, then runs the workers
until Ctrl-C. To poll right away instead of waiting for the schedule:

```sh
lhctl run rust-check-inbox
```

Find an email's run with `lhctl get wfRun <message-id>`.
