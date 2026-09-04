# Wait For Condition

This is a wait for condition example, which does three things:

1. Declare a `counter` variable of type Integer.
2. Wait until the counter reaches 0.
3. An interrupt handler decrements the counter variable when an external
   event is received.

Built with the `sdk-js` **wfsdk**: the `WfSpec` is defined in TypeScript and
registered from code, no checked-in JSON, no `lhctl deploy`.

## Prerequisites

A running LittleHorse server; see [`../../README.md`](../../README.md) for the
one-command setup. Examples read `~/.config/littlehorse.config` when it
exists, else they connect to `localhost:2023`.

## Run it

Start the registrar. It registers the ExternalEventDef and the WfSpec; there
are no TaskDefs in this example:

```bash
npm install
npm start
```

In another terminal, run the workflow:

```bash
lhctl run example-wait-for-condition counter 1
```

Then trigger the interrupt handler with:

```bash
lhctl postEvent <wf_run_id> subtract
```

Check the result:

```bash
lhctl get wfRun <wf_run_id>
```

No `lhctl`? `npm run trigger` drives the whole scenario: it starts a run
with `counter 1`, posts one `subtract` event, and prints the result.
