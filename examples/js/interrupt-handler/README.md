# Interrupt Handler

This example shows how to interrupt a workflow execution.
You need to register an interrupt handler with
`thread.registerInterruptHandler` and declare the ExternalEvent payload type
using `withEventType()`.

In this example the parent thread sleeps 30 sec, we can interrupt it while it
is sleeping, and it creates a child thread. When an interruption is received
the workflow executes the task "some-task"; the task fails, so the whole
workflow fails.

The workflow sleeps for 30 seconds so we have time to post the interruption
event before the workflow finishes.

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

In another terminal, run the workflow and interrupt it:

```bash
# Run a new workflow
lhctl run example-interrupt-handler

# Take the resulting workflow ID; note that it is 'RUNNING':
lhctl get wfRun <wf_run_id>

# Next, post an external event:
lhctl postEvent <wf_run_id> interruption-event STR hello

# Then inspect the wfRun; note that threadRun number 1 is 'ERROR'
# with type 'INTERRUPT':
lhctl get wfRun <wf_run_id>
```

No `lhctl`? `npm run trigger` drives the whole scenario: it starts one run,
posts the interrupt, and prints the result.
