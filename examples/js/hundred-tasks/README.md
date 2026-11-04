# Hundred Tasks

Run a hundred TaskRuns in a single WfRun.

Built with the `sdk-js` **wfsdk**: the `WfSpec` is defined in TypeScript and
registered from code, no checked-in JSON, no `lhctl deploy`.

This workflow doesn't do anything particularly useful other than run a
hundred `TaskRun`s (25 loops over `task-1` through `task-4`) so you can
stress test and tune the server.

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
lhctl run hundred-tasks
```

Check the result:

```bash
lhctl get wfRun <wf_run_id>
```

No `lhctl`? `npm run trigger` starts one run and prints the result.

## Informal benchmarking

To run 100,000 tasks:

```bash
for i in $(seq 1 1000)
do
    lhctl run hundred-tasks
done
```

Note the time you ran the command, then get the last `WfRun` and note the
time it completed. Your tasks per second per stream thread is:

```
100,000 / (<number of seconds> * <number of stream threads>)
```

`number of stream threads` depends on your server config; the default local
setups use one stream thread.
