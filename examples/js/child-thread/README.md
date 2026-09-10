# Child Thread

In this example you will see how to instantiate a child thread
and then wait until it has finished its execution before
executing another task. The child thread in turn spawns a
grandchild thread, demonstrating a three-level hierarchy:
parent, then child, then grandchild.

We use the `thread.spawnThread()` function for that, and
`thread.waitForThreads()` to join each level back in.

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
lhctl run example-child-thread parent-var 2
```

Check the result:

```bash
lhctl get wfRun <wf_run_id>
```

No `lhctl`? `npm run trigger` starts one run and prints the result.
