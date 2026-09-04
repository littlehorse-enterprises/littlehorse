# User Tasks

User Tasks are a type of `Node` in LittleHorse which allow you to assign a
task (in this case, filling out a form) to a human. This example mimics a
common corporate workflow in which some employee requests an item from the IT
department, and the finance department must approve the purchase first:

* A User Task is assigned to the `user-id` who initiated the `WfRun`, which
  involves filling out a description of the requested item and a
  justification. If it is not completed in a minute, it is released to
  `testGroup`.
* A User Task is assigned to the `finance` User Group. It contains notes
  built from the output of the first form, a reminder task that sends the
  finance team an email, and one field: a boolean which determines whether
  the purchase is approved.
* The requester is then notified by email whether the request was approved
  or denied.

Built with the `sdk-js` **wfsdk**: the `WfSpec` is defined in TypeScript and
registered from code, no checked-in JSON, no `lhctl deploy`.

## Prerequisites

A running LittleHorse server; see [`../../README.md`](../../README.md) for the
one-command setup. Examples read `~/.config/littlehorse.config` when it
exists, else they connect to `localhost:2023`.

## Run it

Start the worker. It registers the TaskDef, both UserTaskDefs, and the
WfSpec, then keeps polling:

```bash
npm install
npm start
```

In another terminal, run the workflow:

```bash
lhctl run it-request user-id anakin
```

A run parks on each UserTask node until someone completes the form. With
`lhctl` you can find, assign, and execute them by hand:

```bash
lhctl search userTaskRun --userId anakin --userTaskStatus ASSIGNED
lhctl execute userTaskRun <wf_run_id> <user_task_guid>

lhctl search userTaskRun --userGroup finance --userTaskStatus UNASSIGNED
lhctl assign userTaskRun <wf_run_id> <user_task_guid> --userId mace
lhctl execute userTaskRun <wf_run_id> <user_task_guid>
```

Check the result:

```bash
lhctl get wfRun <wf_run_id>
```

No `lhctl`? `npm run trigger` drives the whole scenario: it starts a run,
completes the request form as `anakin`, answers the approval form as `mace`,
and prints the result.
