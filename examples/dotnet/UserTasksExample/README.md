# Struct-backed user tasks

This example implements an IT purchase request followed by Finance approval.
`ItemRequestForm` and `ApprovalForm` use `[LHStructDef]` and `[LHStructField]`
properties. Startup registers those StructDefs, then passes their returned IDs to
`UserTaskSchema` as the minimum output schemas for the UserTaskDefs.

The workflow stores the first user task's output in a Struct variable and uses
`Get("requestedItem")` and `Get("justification")` to build Finance's notes. It reads
`Get("isApproved")` from the second user task's Struct output to choose an approval
or denial email. Emails are printed by the worker; nothing is actually sent.

## Start the example

Start a local server using the [local development instructions](../../../local-dev/README.md).
You also need the .NET 6 SDK. From the repository root:

```bash
go -C lhctl install .
export PATH="$(go env GOPATH)/bin:$PATH"
lhctl whoami
dotnet build examples/dotnet/UserTasksExample
dotnet run --project examples/dotnet/UserTasksExample --no-build
```

Leave the worker running. It reads `~/.config/littlehorse.config` when present and
registers the TaskDef, StructDefs, UserTaskDefs, and WfSpec before polling for tasks.

## Request an item

In a second terminal, use the same CLI installation:

```bash
export PATH="$(go env GOPATH)/bin:$PATH"
lhctl run it-request user-id anakin
lhctl list userTaskRun <wf_run_id>
lhctl get userTaskRun <wf_run_id> <request_guid>
```

Use the ID returned by `run` and the GUID from `list`. The task is initially
`ASSIGNED` to `anakin`. After 60 seconds without completion, it is released to
`testGroup` and becomes `UNASSIGNED`.

The run's `resultStructDefId` identifies the pinned form schema. Inspect it with:

```bash
lhctl get structDef item-request-form <version>
```

Use the version from the run. Clients should render this schema, which contains
`requestedItem` and `justification`, both strings.

### Save a draft

Before completing the request:

```bash
cat > /tmp/it-request-progress.json <<'JSON'
{"requestedItem": "the rank of master"}
JSON
lhctl save userTaskRun --wfRunId <wf_run_id> --userTaskGuid <request_guid> --resultFile /tmp/it-request-progress.json
lhctl get userTaskRun <wf_run_id> <request_guid>
```

Enter `anakin` as the user ID and `0` as the assignment policy. The draft appears
in `output.struct.struct.fields`, while the task remains unfinished. Saving
replaces the previous draft, so send every field you want to retain.

### Complete the request

```bash
lhctl execute userTaskRun <wf_run_id> <request_guid>
```

Enter `anakin`. The .NET Struct mapper includes property defaults (empty strings
and `false`) in the schema, so the CLI first asks whether to include each field.
Answer `y` to each `Include ...?` prompt, then enter the values in alphabetical order:

| Field | Value |
| --- | --- |
| justification | it's not fair to be on this council and not be a Master! |
| requestedItem | the rank of master |

Submit both fields, including the previously saved value. Fetching the task now
shows `DONE` and a Struct output with the pinned StructDef ID.

## Finance approval

```bash
lhctl list userTaskRun <wf_run_id>
lhctl get userTaskRun <wf_run_id> <approval_guid>
```

Use the second task's GUID. It starts `UNASSIGNED` in `finance`, with notes containing
the submitted item and justification. Its pinned schema is `approval-form`.
After two seconds, the worker prints the reminder message:

```text
Hi finance team, you have a new assigned task
```

Assign and complete it:

```bash
lhctl assign userTaskRun <wf_run_id> <approval_guid> --userId mace
lhctl execute userTaskRun <wf_run_id> <approval_guid>
```

Enter `mace`, answer `y` to `Include isApproved?`, then enter `true`. The worker prints:

```text
Dear anakin, your request for the rank of master has been approved!
```

With `false`, it prints:

```text
Dear anakin, your request for the rank of master has been denied.
```

The Finance task is reassigned to `test-eduwer` 60 seconds after assignment to an
individual. If that deadline passes, complete as `test-eduwer`, or reassign to
`mace` with `--overrideClaim`.

Verify completion and inspect worker results:

```bash
lhctl get wfRun <wf_run_id>
lhctl list taskRun <wf_run_id>
lhctl get taskRun <wf_run_id> <task_guid>
```

The workflow reaches `COMPLETED`. Stop the worker with Ctrl+C when finished.
