# Running a User Tasks Example

The workflow assigns the form to `writer-group` and schedules a reminder after
10 seconds. Once a user completes the form, the workflow passes its entire Struct
output to the `greet` worker, which receives a typed `PersonDetails` object.

Let's run the example:

Start a local LittleHorse server (see [local development](../../../local-dev/README.md)).
From the repository root, install this branch's CLI and Python SDK:

```bash
cd sdk-python
poetry install
poetry run python ../examples/python/user_tasks/user_tasks.py
```

In another terminal, use `lhctl` to run the workflow:

In another terminal:

```bash
lhctl run example-user-tasks
lhctl list userTaskRun <wf_run_id>
lhctl get userTaskRun <wf_run_id> <user_task_guid>
```

Use the workflow ID returned by `run` and the user task GUID returned by `list`.
The user task starts `UNASSIGNED` in `writer-group`. Inspect that schema with:

```bash
lhctl get structDef person-details-form <version>
```

Use the version from `resultStructDefId`. Clients should render the fields from
this pinned schema. After 10 seconds, the worker prints:

```text
Reminder: complete person details for Sam.
```

## Save progress before completing

```bash
cat > /tmp/person-details-progress.json <<'JSON'
{"address": "NA-Street"}
JSON
lhctl save userTaskRun --wfRunId <wf_run_id> --userTaskGuid <user_task_guid> --resultFile /tmp/person-details-progress.json
```

Enter `sam` when asked for the user ID and `0` for the overwrite policy.
Fetching the user task again shows the draft in `output.struct.struct.fields`.
Saving progress does not complete the task. Each save replaces the previous draft;
send all fields you want to keep.

## Complete the form

```bash
lhctl execute userTaskRun <wf_run_id> <user_task_guid>
```

Enter `sam` as the user ID. The CLI prompts for the Struct fields in alphabetical
order:

| Field | Value |
| --- | --- |
| address | NA-Street |
| age | 28 |
| identification | 1258796641-4 |

Completion submits all required fields, including any previously saved values.
The worker prints:

```text
Hello Sam! WfRun <wf_run_id> Person: identification=1258796641-4, address=NA-Street, age=28
```

Verify the result:

```bash
lhctl get userTaskRun <wf_run_id> <user_task_guid>
lhctl get wfRun <wf_run_id>
lhctl list nodeRun <wf_run_id>
lhctl list taskRun <wf_run_id>
lhctl get taskRun <wf_run_id> <task_run_guid>
```

The user task is `DONE`, its output is a Struct with the run's pinned StructDef ID,
and the workflow eventually reaches `COMPLETED`. Use the task run GUIDs returned
by `list` to inspect the reminder or greeting result.
