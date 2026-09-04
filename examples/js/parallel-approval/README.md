# Parallel Approval

Uses the "Wait for Threads" feature of LittleHorse to simulate a business
process in which:

* Three people must signal approval for some arbitrary transaction, and
* We want to periodically execute some task (eg. a reminder) until all three
  approvals have been gathered.

Each approval is its own child `ThreadRun` waiting for a `person-N-approves`
external event. A fourth child thread sends reminders on a loop until the
`all-approved` variable flips to `true`. If any approval comes back with
`{"approval": false}`, that child thread fails with `denied-by-user` and the
exception handler propagates the failure, leaving the `WfRun` in the
`EXCEPTION` state.

Built with the `sdk-js` **wfsdk**: the `WfSpec` is defined in TypeScript and
registered from code, no checked-in JSON, no `lhctl deploy`.

## Prerequisites

A running LittleHorse server; see [`../../README.md`](../../README.md) for the
one-command setup. Examples read `~/.config/littlehorse.config` when it
exists, else they connect to `localhost:2023`.

## Run it

Start the worker. It registers the TaskDefs, the ExternalEventDefs and the
WfSpec, then keeps polling:

```bash
npm install
npm start
```

In another terminal, run the workflow:

```bash
lhctl run parallel-approval
```

Note that there are five `ThreadRun`s: the entrypoint, the reminder thread,
and one child thread for each of the three required approvals. Post the
approvals:

```bash
lhctl postEvent <wf_run_id> person-2-approves JSON_OBJ '{"approval": true}'
lhctl postEvent <wf_run_id> person-1-approves JSON_OBJ '{"approval": true}'
lhctl postEvent <wf_run_id> person-3-approves JSON_OBJ '{"approval": true}'
```

The reminder thread wakes up about 20 seconds later, sees `all-approved` is
`true`, and exits instead of sending the next reminder. Check the result:

```bash
lhctl get wfRun <wf_run_id>
lhctl get variable <wf_run_id> 0 all-approved
```

To see the failure path, run the workflow again and deny one approval:

```bash
lhctl postEvent <wf_run_id> person-1-approves JSON_OBJ '{"approval": false}'
```

The `WfRun` ends up in the `EXCEPTION` state because the exception handler
propagated the `denied-by-user` failure up the stack.

No `lhctl`? `npm run trigger` starts one run, posts all three approvals and
prints the result (it takes about 20 seconds while the reminder thread
sleeps).
