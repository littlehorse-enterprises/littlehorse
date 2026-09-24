# Run Child Workflow Node

Invoke another `WfSpec` as a child using `runWf()`, then block on it with
`waitForChildWf()`. The child (`some-other-wfspec`) is an ordinary `WfSpec`
with no knowledge of its parent (`my-parent`); compare this with the
`hierarchical-workflow` example, where the child depends on being a child and
even reads variables from the parent.

Built with the `sdk-js` **wfsdk**: the `WfSpec` is defined in TypeScript and
registered from code, no checked-in JSON, no `lhctl deploy`.

## Prerequisites

A running LittleHorse server; see [`../../README.md`](../../README.md) for the
one-command setup. Examples read `~/.config/littlehorse.config` when it
exists, else they connect to `localhost:2023`.

## Run it

Start the worker. It registers the TaskDef and both WfSpecs (`my-parent` and
`some-other-wfspec`), then keeps polling:

```bash
npm install
npm start
```

In another terminal, run the parent workflow:

```bash
lhctl run my-parent input-name colt
```

To see that `some-other-wfspec` really isn't special, you can run it on its
own:

```bash
lhctl run some-other-wfspec child-input-name colt
```

Check the result:

```bash
lhctl get wfRun <wf_run_id>
```

Find the child `WfRun` spawned by the parent, then fetch it with
`lhctl get wfRun <parent-wf-run-id>_<child-wf-run-id>`:

```bash
lhctl search wfRun byParent <wf_run_id>
```

No `lhctl`? `npm run trigger` starts one run and prints the result.
