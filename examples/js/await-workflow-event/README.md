# Await Workflow Event

Throw a `WorkflowEvent` from a `WfRun` with `throwEvent()`, and block on it
from the outside with the `rpc AwaitWorkflowEvent`. The `WfSpec` in this
example simply sleeps for a configurable number of seconds determined by the
`sleep-time` input variable, and then throws a `sleep-done` `WorkflowEvent`.

Built with the `sdk-js` **wfsdk**: the `WfSpec` is defined in TypeScript and
registered from code, no checked-in JSON, no `lhctl deploy`.

## Prerequisites

A running LittleHorse server; see [`../../README.md`](../../README.md) for the
one-command setup. Examples read `~/.config/littlehorse.config` when it
exists, else they connect to `localhost:2023`.

## Run it

Start the worker. It registers the `sleep-done` WorkflowEventDef and the
WfSpec (there are no TaskDefs in this example), then stays up:

```bash
npm install
npm start
```

In another terminal, run the workflow:

```bash
lhctl run await-wf-event sleep-time 1
```

Check the result:

```bash
lhctl get wfRun <wf_run_id>
```

No `lhctl`? `npm run trigger` starts one run and prints the result. It plays
out the "WorkflowEvent arrives first" scenario from the Java example: it runs
the workflow with `sleep-time` 1, waits 3000 milliseconds, then calls
`awaitWorkflowEvent` with a 1000 millisecond timeout and prints the event it
gets back. The other two scenarios are the RPC arriving before the event
(the call blocks until the event is thrown) and the timeout firing first
(the call fails with `DEADLINE_EXCEEDED`).
