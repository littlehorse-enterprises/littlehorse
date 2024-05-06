---
sidebar_label: LittleHorse CLI
---

# LittleHorse CLI

`lhctl` is the LittleHorse CLI. It allows you to manage metadata in your system, observe and analyze your `WfRun`s, and also perform rudimentary actions such as running a `WfRun`.

## Create Metadata

The `lhctl deploy` command allows you to create metadata such as `WfSpec`, `TaskDef`, `UserTaskDef`, and `ExternalEventDef`.

The syntax of the command is as follows:

```
lhctl deploy {wfSpec,taskDef,externalEventDef} <file>
```

The `<file>` parameter is expected to be a JSON-formatted printout of the corresponding protobuf. If your file is in the binary form, you can deploy using the `--proto` flag.

The following creates a simple `TaskDef`:

```
-> cat <<EOF > /tmp/taskDef.json
{
    "name": "my-task",
    "inputVars": [{
	    "type": "STR",
	    "name": "my-input-var"
    }]
}

-> lhctl deploy taskDef /tmp/taskDef.json
{
  "code":  "OK",
  "result":  {
    "name":  "my-task",
    "inputVars":  [
      {
        "type":  "STR",
        "name":  "my-input-var",
        "required":  false
      }
    ],
    "createdAt":  "2023-04-18T18:54:23.263Z"
  }
}

```

The same syntax can be used for creating `WfSpec`, `UserTaskDef`, and `ExternalEventDef` objects.

## View Objects

The `lhctl get` command allows you to inspect the state of an API Object; `lhctl search` allows you to find the ID's of API Object which match certain criteria; and `lhctl list` allows you to retrieve multiple API Objects at one time.

The following sections describe how to interact with each type of API Object.

### Common Notes

In `lhctl search`, there are two global flags:

- `bookmark`
- `limit`

The `limit` flag is quite self-explanatory as it simply limits the number of results for a request.

All `lhctl search` responses have an optional `bookmark` field, which is a base64-encoded pagination token. If all results that match the search have been returned, then no `bookmark` is provided. Otherwise, if you wish to retrieve more results starting from where you left off, just pass in the provided `bookmark` by copying and pasting the base64-encoded data.

### `WfSpec`

`WfSpec`s are versioned objects, meaning that their ID comprises a `name` and a `version`. When a new `WfSpec` is created with the same `name` as an older one, it gets an incremented `version` number and lives in the API as its own separate object.

You can retrieve the latest `WfSpec` named `foo` by doing:

```
lhctl get wfSpec foo
```

You can retrieve a list of all `WfSpec`s named `foo` by doing:

```
-> lhctl search wfSpec --name foo
{
  "results":  [
    {
      "name":  "foo",
      "version":  0
    },
    {
      "name":  "foo",
      "version":  1
    }
  ],
}
```

To get an old version (eg. `0`) of a `WfSpec` named `foo`, you can:

```
lhctl get wfSpec foo --v 0
```

### `WfRun`

To get a `WfRun` with id `<my-wf-id>` you can:

```
lhctl get wfRun <my-wf-id>
```

#### Specific `WfSpec` Version

You can search for `WfRun`s by providing the name and version of the `WfSpec` and the status of the `WfRun`. For example, if you want to find all failed `WfRun`'s from the `foo` WfSpec, version `9`, you would do the following:

```
lhctl search wfRun --wfSpecName foo --wfSpecVersion 9 --status ERROR
```

#### By `wfSpecName` and `status`

You can search for `WfRun`s by providing the name of the `WfSpec` and the `status` of the `WfRun`. This retrieves results from all versions of the `WfSpec`:

```
lhctl search wfRun --wfSpecName foo --status ERROR
```

#### By `wfSpecName`

If you only specify `--wfSpecName`, `WfRun`s with any status and any version of the provided `WfSpec` are returned:

```
lhctl search wfRun --wfSpecName foo
```

#### By Time

Every flavor of `lhctl search wfRun` shown above also allows you to filter based on the _time that the `WfRun` was launched_ via the following options:

- `--earliestMinutesAgo`: Only show `WfRun`s more recent than this configuration.
- `--latestMinutesAgo`: only show `WfRun`'s less recent than this configuration.

For example, to find all workflows started between 10 and 15 minutes ago that are in the `COMPLETED` state, we would do:

```
lhctl search wfRun --wfSpecName foo --status COMPLETED --earliestMinutesAgo 15 --latestMinutesAgo 10
```

### `NodeRun`

A `NodeRun` has a composite ID consisting of the `wfRunId`, `threadRunNumber`, and `position`.

To get all NodeRun associated to an specific WfRun:

```
lhctl list nodeRun <wfRunId>
```

Get an simplified response as follows:

```
lhctl search nodeRun --wfRunId <my-wf-id>
```

Use `lhctl get nodeRun` to find an specific NodeRun, The syntax for this is as follows:

```
lhctl get nodeRun <wfRunId> <threadRunNumber> <position>
```

To get the second (zero-indexed) `NodeRun` for the entrypoint thread (Thread Run `0`) of the `WfRun` with id `123foo`, you can:

```
lhctl get nodeRun 123foo 0 2
```

### `TaskDef` and `ExternalEventDef`

The `lhctl` syntax for `TaskDef` and `ExternalEventDef` is identical. You can get a specific `TaskDef` or `ExternalEventDef` by its `name` using:

```
lhctl get taskDef <name>
```

You can list all `TaskDef`s or `ExternalEventDef`s as follows:

```
lhctl search externalEventDef
```

You can search `TaskDef`s and `ExternalEventDef`s with prefixes as follows:

```
lhctl search taskDef --prefix some-pref
```

### `TaskRun`

A `TaskRun` is a running instance of a Task in LittleHorse. A `TaskRun` is associated with a `TaskDef`.

To retrieve all `TaskRun`'s associated with a `TaskDef`, use the following command:

```
lhctl search taskRun --taskDefName <TaskDefName>
```

You can also filter `TaskRun`s by specifying a particular `TaskRun` status:

```
lhctl search taskRun --taskDefName <TaskDefName> --status <TaskRunStatus>
```

Possible values for the status parameter are as follows:

- `TASK_SCHEDULED`
- `TASK_RUNNING`
- `TASK_SUCCESS`
- `TASK_FAILED`
- `TASK_TIMEOUT`
- `TASK_OUTPUT_SERIALIZING_ERROR`
- `TASK_INPUT_VAR_SUB_ERROR`

Just like `lhctl search wfRun`, each flavor of `lhctl search taskRun` shown above also allows you to filter based on the _time that the `taskRun` was scheduled_ via the following options:

- `--earliestMinutesAgo`: Only show `TaskRun`s more recent than this configuration.
- `--latestMinutesAgo`: only show `TaskRun`'s less recent than this configuration.

A `TaskRun` has a composite id consisting of the `id` of its `WfRun`, and a `taskGuid`. To get a `TaskRun`, you can use:

```
lhctl get taskRun <wfRunId> <taskGuid>

# This is equivalent:
lhctl get taskRun <wfRunId>/<taskGuid>
```

### `UserTaskRun`

A `UserTaskRun` is an instance of a `UserTaskDef` in LittleHorse, in which a human is assigned a Task and the `WfRun` blocks until that Task is completed. The ID of a `UserTaskRun` is a composite ID consisting of the ID of the `WfRun` and a Guid.

To get a `UserTaskRun`, you can run:

```
lhctl get userTaskRun <wfRunId> <userTaskGuid>
```

You can search for `UserTaskRun`'s with multiple combinations of flags. Supported flags for a `UserTaskRun` search are:

- `--earliestMinutesAgo`
- `--latestMinutesAgo`
- `--userTaskStatus`, which is the status of the `UserTaskRun`. Valid values are:
  - `UNASSIGNED`
  - `ASSIGNED`
  - `DONE`
  - `CANCELLED`
- `--userId`, or the ID of the User to whom the Task is assigned.
- `--userGroup`, or the ID of the User Group to whom the Task is assigned.
- `--userTaskDefName`, or the name of the `UserTaskDef` that the Task comes from.

### `UserTaskDef`

A `UserTaskDef` is equivalent to a `TaskDef` but for `UserTaskRun`s instead of `TaskRun`s. To find all `UserTaskDef`s, you can:

```
lhctl search userTaskDef

# By Prefix
lhctl search userTaskDef --prefix some-prefix-
```

You can get a `userTaskDef` as follows:

```
lhctl get userTaskDef my-user-task-def

# optionally specify version. If version not set, defaults to latest.
lhctl get userTaskDef my-user-task-def --v 2
```

You can delete a `UserTaskDef` as follows:

```
lhctl delete userTaskDef my-user-task-def 2  # version is required
```

### `ExternalEvent`

An `ExternalEvent` has a composite ID consisting of:

- The associated `wfRunId`.
- The `name` of the `ExternalEventDef`.
- A unique `guid` for that `ExternalEvent` instance.

To get an `ExternalEvent`, you can:

```
lhctl search externalEvent <wfRunId> <externalEventDefName> <guid>
```

You can list all `ExternalEvent`s for a given `wfRunId` via the following:

```
lhctl search externalEvent --wfRunId <wfRunId>
```

### `Variable`

A `Variable` has a composite ID consisting of:

- The associated `wfRunId`.
- The `threadRunNumber` of the owning ThreadRun.
- The `name` of the Variable.

You can get a specific variable via:

```
lhctl get variable <wfRunId> <threadRunNumber> <name>
```

You can list all `Variable`s for a given `WfRun` via:

```
lhctl search variable --wfRunId <wfRunId>
```

You can search for `Variable`s with a certain value (not supported for `JSON_OBJ`, `BYTES`, and `JSON_ARR` variables). You must pass in the Variable `name`, the type, and the value. For example, to search for `email-address` variable's with the value `foo@bar.com`, you would:

```
lhctl search variable --varType STR --value 'foo@bar.com' --name email-address
```

Supported variable types for searching are:

- `STR`
- `INT`
- `DOUBLE`
- `BOOL`

### Metrics

The LittleHorse CLI lets you view metrics in a rudimentary manner. It is recommended to use the Admin Dashboard to better visualize these metrics; however, you can still view metrics through the CLI.

LittleHorse exposes two types of metrics: "Task Metrics", which are aggregated by `TaskDef`, and "Workflow Metrics", which are aggregated by `WfSpec`.

Metrics are collected and aggregated on tumbling time windows. There are three sizes of windows which you can use:

- `MINUTES_5`,
- `HOURS_2`, and
- `DAYS_1`.

To get the last 10 windows of size `MINUTES_5` of metrics for the `foo-task` TaskDef, you can:

```
lhctl taskDefMetrics foo-task MINUTES_5 10
```

To get the last 5 windows of size `DAYS_1` for the second version of the `foo-wf` WfSpec, you can:

```
lhctl wfSpecMetrics foo-wf 2 DAYS_1 5
```

## Manage `WfRun`s

`lhctl` allows you to perform basic actions around running, stopping, and resuming `WfRun`s and also creating `ExternalEvent`s.

### Run a `WfRun`

You can run a `WfRun` using the `lhctl run` command. The syntax is:

```
lhctl run <wfSpecName> ...args
```

All positional arguments after the WfSpec Name are interpreted as pairs of
{Variable Name, Variable Value}. The variable values are intelligently deserialized
to their appropriate types; for example, if var 'foo' is of type 'JSON_OBJ', then
the argument '{"bar":"baz"}' will be unmarshalled as a JSON object.

To run the "my-workflow" `WfSpec` with two input parameters,

- `foo` set to the Json Object `{"bar":"baz"}`
- `my-int` set to `123`

you can:

```
lhctl run my-workflow foo '{"bar":"baz"}' my-int 123
```

You can also set the ID of the `WfRun` using the `--wfRunId` flag. Note that there can only be one `WfRun` with a given ID. This can be used to guarantee idempotence.

### Stop and Resume a `WfRun`

You can use:

```
lhctl stop wfRun <wfRunId>
```

to stop a `WfRun`, and then resume it with:

```
lhctl resume wfRun <wfRunId>
```

You can optionally stop or resume a child thread (without affecting the entrypoint ThreadRun) by using the `--threadRunNumber` argument.

### Post an `ExternalEvent`

`lhctl postEvent` allows you to post an ExternalEvent of a specified Event Type and Variable Type to a WfRun. Specifying the Variable Type for the external event is currently required as ExternalEventDef's currently do not carry Schema information (this will change in a future release). The payload is deserialized according to the type. JSON objects should be provided as
a string; BYTES objects should be b64-encoded.

To send an External Event of type `my-event` with a String value `"my-event-content"` to the `WfRun` given by id "my-wf-id", you can:

```
lhctl postEvent my-wf-id my-event STR my-event-content
```

As a refresher, the valid variable types in LittleHorse are:

- STR
- INT
- DOUBLE
- BOOL
- JSON_OBJ
- JSON_ARR
- BYTES

You can optionally specify the `--guid` flag to guarantee idempotence of this request, as only one `ExternalEvent` can exist with the same `guid`, `wfRunId`, and `externalEventDefName`.

You can optionally assign the `ExternalEvent` to a specific `NodeRun` or `ThreadRun` using the `--nodeRunPosition` and `--threadRunNumber` flags, respectively. The `--nodeRunPosition` flag is only valid if the `--threadRunNumber` flag is also set.
