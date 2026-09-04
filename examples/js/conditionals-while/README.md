# Conditionals While

In this example you will see how to define a while loop with LH.
Use `doWhile(condition, whileBody)` to define the loop,
the end condition and the handler (body) of the while.

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
lhctl run example-conditionals-while number-of-donuts 5
```

Check the result:

```bash
lhctl get wfRun <wf_run_id>
```

No `lhctl`? `npm run trigger` starts one run and prints the result.
