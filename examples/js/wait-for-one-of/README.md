# Wait for One Of

Shows how `waitForAnyOf()` works: spawn multiple child `ThreadRun`s and
continue once any of them is done. Each child waits for a different external
event, and the first event to arrive unblocks the parent.

Built with the `sdk-js` **wfsdk**: the `WfSpec` is defined in TypeScript and
registered from code, no checked-in JSON, no `lhctl deploy`.

## Prerequisites

A running LittleHorse server; see [`../../README.md`](../../README.md) for the
one-command setup. Examples read `~/.config/littlehorse.config` when it
exists, else they connect to `localhost:2023`.

## Run it

Start the worker. It registers the TaskDef, the ExternalEventDefs and the
WfSpec, then keeps polling:

```bash
npm install
npm start
```

In another terminal, run the workflow:

```bash
lhctl run example-wait-for-one-of --wfRunId my-wf-run
```

Now you can choose whether to complete child thread 1 or child thread 2:

```bash
lhctl postEvent my-wf-run child-1-event STR "hello"
```

Or

```bash
lhctl postEvent my-wf-run child-2-event STR "hello"
```

Check the result:

```bash
lhctl get wfRun my-wf-run
```

No `lhctl`? `npm run trigger` starts one run, posts `child-1-event` and
prints the result.
