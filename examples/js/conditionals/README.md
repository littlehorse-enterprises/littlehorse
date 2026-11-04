# Conditionals

In this example you will see how to use conditionals.
It will execute an "if" or "else" depending on the value of "bar".
If bar is greater than 10 then execute task-b else execute task-c.

Built with the `sdk-js` **wfsdk**: the `WfSpec` is defined in TypeScript and
registered from code, no checked-in JSON, no `lhctl deploy`.

## Prerequisites

A running LittleHorse server; see [`../../README.md`](../../README.md) for the
one-command setup. Examples read `~/.config/littlehorse.config` when it
exists, else they connect to `localhost:2023`.

## Run it

Start the worker. It registers the TaskDefs and the WfSpec, then keeps polling:

```bash
npm install
npm start
```

In another terminal, run the workflow:

```bash
# Execute task-b
lhctl run example-conditionals foo '{"bar": 15}'

# Execute task-c
lhctl run example-conditionals foo '{"bar": 5}'
```

Check the result:

```bash
lhctl get wfRun <wf_run_id>
```

No `lhctl`? `npm run trigger` runs both branches and prints each result.
