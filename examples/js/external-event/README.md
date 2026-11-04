# External Event

This example demonstrates the asynchronous ExternalEvent functionality.
We use `thread.waitForEvent` to wait for an external event, then when it
arrives the workflow executes the task "greet".

Built with the `sdk-js` **wfsdk**: the `WfSpec` is defined in TypeScript and
registered from code, no checked-in JSON, no `lhctl deploy`.

## Prerequisites

A running LittleHorse server; see [`../../README.md`](../../README.md) for the
one-command setup. Examples read `~/.config/littlehorse.config` when it
exists, else they connect to `localhost:2023`.

## Run it

Start the worker. It registers the TaskDefs, the ExternalEventDef, and the
WfSpec, then keeps polling:

```bash
npm install
npm start
```

In another terminal, run the workflow and post the event:

```bash
# Start workflow
lhctl run example-external-event

# Take the resulting workflow ID; note that it is 'RUNNING':
lhctl get wfRun <wf_run_id>

# Next, post an external event:
lhctl postEvent <wf_run_id> name-event STR Obi-Wan

# Then inspect the wfRun; note it is 'COMPLETED':
lhctl get wfRun <wf_run_id>
```

No `lhctl`? `npm run trigger` drives the whole scenario: it starts one run,
posts the event, and prints the result.
