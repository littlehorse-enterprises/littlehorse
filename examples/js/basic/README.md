# Basic

A simple example, which does two things:

1. Declare an "input-name" variable of type String
2. Pass that variable into the execution of the "greet" task.

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
lhctl run example-basic input-name Obi-Wan
```

Check the result:

```bash
lhctl get wfRun <wf_run_id>
```

No `lhctl`? `npm run trigger` starts one run and prints the result.
