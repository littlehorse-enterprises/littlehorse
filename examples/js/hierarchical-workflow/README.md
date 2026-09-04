# Hierarchical Workflow

A three-level workflow hierarchy with `parent`, `child` and `grand-child`
`WfSpec`s. The `child` `WfSpec` uses a `Variable` defined in the `parent`,
and the `grand-child` inherits from the `child`. The variable visibility
methods (`asPublic()`, `asInherited()`) let variables flow across multiple
levels of the hierarchy.

There is a variable called `name` defined in the `parent` `WfSpec`. The
`child` refers to that parent variable and does the following:

* Passes it as a parameter into the `greet` task.
* At the end of the workflow, mutates it and sets it to `"yoda"`.

The `grand-child` extends the hierarchy further by:

* Inheriting from the `child` workflow.
* Waiting for an external event (`some-event`) to complete its execution.

Built with the `sdk-js` **wfsdk**: the `WfSpec`s are defined in TypeScript and
registered from code, no checked-in JSON, no `lhctl deploy`.

## Prerequisites

A running LittleHorse server; see [`../../README.md`](../../README.md) for the
one-command setup. Examples read `~/.config/littlehorse.config` when it
exists, else they connect to `localhost:2023`.

## Run it

Start the worker. It registers the TaskDef and all three `WfSpec`s, then
keeps polling:

```bash
npm install
npm start
```

In another terminal, run the parent workflow:

```bash
lhctl run parent name obi-wan --wfRunId my-parent-wf
lhctl get variable my-parent-wf 0 name
```

The value of the variable is `obi-wan`. Next, run the child:

```bash
lhctl run child --parentWfRunId my-parent-wf --wfRunId my-child-wf
lhctl get wfRun my-parent-wf_my-child-wf
lhctl get variable my-parent-wf 0 name
```

The parent variable was set to `yoda`! You can also run the grand-child
workflow, which inherits from the child:

```bash
lhctl run grand-child --parentWfRunId my-child-wf --wfRunId my-grand-child-wf
```

The grand-child waits for an external event. To complete it, send the
expected event and check the status:

```bash
lhctl put external-event my-grand-child-wf some-event '{"message": "Hello from grand-child!"}'
lhctl get wfRun my-grand-child-wf
```

No `lhctl`? `npm run trigger` walks the whole hierarchy once (parent, child,
grand-child plus the event) and prints each result.
