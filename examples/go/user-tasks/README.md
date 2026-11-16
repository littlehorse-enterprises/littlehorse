# User Tasks

This is the Go version of the [Java user task example](../../java/user-tasks).
It uses the Go SDK's Struct-backed user task schemas for the same IT purchase
request workflow:

1. The requester supplies `requestedItem` and `justification`. After 60 seconds,
   the task is released to `testGroup` if it is still assigned to the requester.
2. Finance receives a task with the request details in its notes and an
   `isApproved` field. A fake email reminder runs after 2 seconds if the task is
   still pending. Once assigned to a user, a 60-second deadline starts; if that
   assignment is unchanged and the task remains open, it is reassigned to `test-eduwer`.
3. Approval or denial prints the corresponding email to the requester.

The email worker prints messages; it does not send real emails. The workflow and
task names match Java (`it-request`, `approve-it-request`, and `send-email`). Run
one example's worker at a time, since they use the same TaskDef.

## Run

Start a LittleHorse server and configure `lhctl` and the SDK through
`~/.config/littlehorse.config` or environment variables. Verify connectivity:

```sh
lhctl whoami
```

Use the CLI built from this branch so it supports Struct-backed forms:

```sh
go -C lhctl install .
```

Ensure Go's binary directory is on your `PATH`.

From the repository root, start the worker in a separate terminal and leave it
running. This registers the `send-email` TaskDef:

```sh
go run ./examples/go/user-tasks/worker
```

In another terminal, deploy the StructDefs, UserTaskDefs, and WfSpec, then start a
workflow for `anakin`:

```sh
go run ./examples/go/user-tasks/deploy
lhctl run it-request user-id anakin
lhctl list userTaskRun <wfRunId>
```

The deployment registers each form's StructDef first and uses its returned ID
with `NewUserTaskSchema(...).Compile()`. Each UserTaskRun pins its resolved schema
in `resultStructDefId`. Workflow code uses `Get` to read Struct fields.

## Fill out the request

Use the first task's GUID from the list above:

```sh
lhctl get userTaskRun <wfRunId> <requestGuid>
```

Optionally save a draft **before completing the task**. Create `progress.json`:

```sh
cat > progress.json <<'EOF'
{"requestedItem":"the rank of master"}
EOF
```

```sh
lhctl save userTaskRun --wfRunId <wfRunId> --userTaskGuid <requestGuid> --resultFile progress.json
lhctl get userTaskRun <wfRunId> <requestGuid>
```

Enter `anakin`, then `0` (`FAIL_IF_CLAIMED_BY_OTHER`) for the assignment policy.
The run's `output.struct.struct.fields.requestedItem.value.str` should contain
`the rank of master`, and the task remains open. After 60 seconds it becomes
`UNASSIGNED` in `testGroup`; the draft remains saved. Each save replaces the draft,
so include all fields you want to retain.

Now complete the request:

```sh
lhctl execute userTaskRun <wfRunId> <requestGuid>
```

Enter `anakin` as the user ID, then answer the prompts for `justification` and
`requestedItem`. For example, use "It's not fair to be on this council and not be
a Master!" and "the rank of master". Completion submits a complete form, so fill
both fields even if you previously saved a draft. The request becomes `DONE`.

## Finance approval

After completing the request, list the runs again to find the Finance task:

```sh
lhctl list userTaskRun <wfRunId>
lhctl get userTaskRun <wfRunId> <approvalGuid>
lhctl assign userTaskRun <wfRunId> <approvalGuid> --userId mace
lhctl execute userTaskRun <wfRunId> <approvalGuid>
```

Enter `mace`, then `true` to approve or `false` to deny. The worker prints the same
approval/denial messages as Java. If 60 seconds have passed since user assignment,
the task may be assigned to `test-eduwer`; inspect the run before reassigning it.
To take it back for this demonstration, use:

```sh
lhctl assign userTaskRun <wfRunId> <approvalGuid> --userId mace --overrideClaim
```

```sh
lhctl get wfRun <wfRunId>
```

Once the final email task finishes, the workflow should be `COMPLETED`.

For approval, the worker prints:

```text
Sending email to anakin
Content: Dear anakin, your request for the rank of master has been approved!
```

For denial, it prints `Dear anakin, your request for the rank of master has been denied.`
Start a new workflow to try the other outcome.

## Cancellation

To try cancellation, start a new workflow and cancel its first task:

```sh
lhctl cancel userTaskRun <wfRunId> <requestGuid>
lhctl get wfRun <wfRunId>
```

The task becomes `CANCELLED` and the workflow ends with `ERROR` (`User task cancelled`).
Like Java, this example includes a support-email exception handler. Default
cancellation is a `USER_TASK_CANCELLED` error, so that exception handler does not
run for this command.
