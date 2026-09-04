# Spawn Thread Foreach

In this example you will see how to spawn multiple threads based on an input
JSON array: one child thread runs per element of `$.approvals`, then the
parent waits for all of them and runs one final task.

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
lhctl run spawn-parallel-threads-from-json-arr-variable approval-chain '{"description": "demo for approvals", "approvals":  [{"user": "yoda"}, {"user": "chewbacca"}, {"user": "anakin"}]}'
```

Check the result:

```bash
lhctl get wfRun <wf_run_id>
```

You can also see the input variables to each ThreadRun:

```bash
lhctl get variable <wf_run_id> 1 INPUT  # yoda
lhctl get variable <wf_run_id> 2 INPUT  # chewbacca
lhctl get variable <wf_run_id> 3 INPUT  # anakin
```

No `lhctl`? `npm run trigger` starts one run and prints the result.
