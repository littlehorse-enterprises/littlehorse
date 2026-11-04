# Checkpointed Tasks

A simple example which shows the usage of checkpoints in tasks.

The `example-checkpointed-tasks` workflow declares a searchable `input-name`
variable and passes it into the `greet` task, which runs with 2 retries. The
task wraps two operations in `executeAndCheckpoint`: each checkpointed block
runs once across all attempts of the TaskRun. The first attempt completes the
first checkpoint and then fails on purpose; the retry replays the first
checkpoint's stored result instead of re-executing it, then runs the second
checkpoint and completes.

Built with the `sdk-js` **wfsdk**: the `WfSpec` is defined in TypeScript and
registered from code, no checked-in JSON, no `lhctl deploy`.

## Prerequisites

A running LittleHorse server; see [`../../README.md`](../../README.md) for the
one-command setup. Examples read `~/.config/littlehorse.config` when it
exists, else they connect to `localhost:2023`.

## Run it

Start the worker. It registers the TaskDef and the WfSpec, then keeps polling:

```bash
npm install
npm start
```

In another terminal, run the workflow:

```bash
lhctl run example-checkpointed-tasks input-name "Qui-Gon Jinn"
```

Observe the task worker output to see what's going on.

Check the result:

```bash
lhctl get wfRun <wf_run_id>
```

No `lhctl`? `npm run trigger` starts one run and prints the result.
