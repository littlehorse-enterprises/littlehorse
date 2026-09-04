# Worker Context

This example shows how to get access to the context when executing a task.
The `task` worker receives the `request-time` the caller passed in, computes
how far behind it is running, and logs the WfRun id, the TaskRun guid, and
the attempt number from the `WorkerContext`.

If you need access to the context, add a `WorkerContext` parameter as the
last parameter of your task function:

```ts
function task(requestTime: number, ctx: WorkerContext): void {
  ...
}
```

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
lhctl run example-worker-context request-time $(date +%s%3N)
```

> `date +%s%3N` is a gnu command:
> https://www.gnu.org/software/coreutils/manual/html_node/date-invocation.html

Check the result:

```bash
lhctl get wfRun <wf_run_id>
```

No `lhctl`? `npm run trigger` starts one run and prints the result.
