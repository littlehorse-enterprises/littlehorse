---
sidebar_label: Tasks
---
# `TaskDef` and `TaskRun`

The execution of work is fundamental to any Workflow engine. A unit of work executed by a computer in LittleHorse is represented by the dual objects `TaskDef` and `TaskRun`.

A `TaskDef` is a LittleHorse API Object which defines a certain type of task that can be executed by a computer. A `TaskRun` is a LittleHorse API Object representing an instance of such a task being executed by a computer as part of a `WfRun`.

## In the API

A `TaskDef`'s ID in the API is simply its `name`. Only one `TaskDef` of a given name can exist at any time.

a `TaskRun`'s ID in the API is a composite ID, consisting of:
* The ID of its `WfRun`
* A guid.

A `WfSpec` might have a `Node` of type `TASK`. Such a `Node` will have a `taskDefName` field on it which points to a `TaskDef`.

When a `WfRun` reaches a `TASK` `Node`, a `TaskRun` is created. The associatd `NodeRun` will have a `task` field, containing the ID of the associated `TaskRun`. The status of the `NodeRun` will mirror the status of the associated `TaskRun`.

## `TaskRun` LifeCycle

When a `TaskRun` is created, the LH Server first assigns the `input_variables` for that `TaskRun`. The `input_variables` of the `TaskRun` must match up in terms of name and type with the `input_vars` of the associated `TaskDef`. This mirrors how the arguments to a function invocation in programming must match the method signature.

For a `TASK` `NodeRun`, the output of the `NodeRun` (for use with `Variable` mutations) is determined by the output of the first successful `TaskAttempt`.

### `TaskRun` Status

A `TaskRun` can be in any of the following statuses:

* `TASK_SCHEDULED`: It has been scheduled but a Task Worker has not yet 
* `TASK_RUNNING`: A Task Worker has received the Task but not yet reported the result.
* `TASK_SUCCESS`: The `TaskRun` was completed :slightly_smiling_face:
* `TASK_FAILED`: An unexpected error or exception was encountered.
* `TASK_TIMEOUT`: The Task Worker did not report a result for the `TaskRun` within the allotted timeout.
* `TASK_OUTPUT_SERIALIZING_ERROR`: The Task Worker executed the `TaskRun` but was unable to serialize the result when reporting back to the LH Server.
* `TASK_INPUT_VAR_SUB_ERROR`: The LH Server was unable to calculate the input variables for the `TaskRun`, or the Task Worker was unable to deserialize them and call the actual function.

### Retries and `TaskAttempt`

A `TaskRun` has a `max_attempts` field which is used to determine the number of retries for a `TaskRun`. This is determined by the `TaskNode` structure.

:::note
Multiple different `Node`s and even different `WfSpec`s can use the same `TaskDef`. Since retries are configured at the `TaskNode` level, it is possible for two `TaskRun`s of the same `TaskDef` to have a different maximum number of retries.
:::

When a `TaskRun` is first created, a `TaskAttempt` is also created. If the `TaskAttempt` comes back with a `TASK_SUCCESS` status, then great! The `TaskRun` is completed, and if it is associated with a `TASK` `NodeRun`, then the output of the `NodeRun` is just the output of the `TaskAttempt`.

A `TaskAttempt` is considered retryable if it ends with the following states:
* `TASK_FAILED`, denoting an exception.
* `TASK_TIMEOUT`, denoting that the Task Worker did not report the result of the `TaskAttempt` in time.

If a `TaskAttempt` is retryable and there are sufficient retries left, then another `TaskAttempt` _within the same `TaskRun`_ is created. If any of the retry `TaskAttempt`s succeed, then the output of the `TASK` `NodeRun` is the output of the first successful `TaskAttempt`. If all fail, then `NodeRun` fails.

### Interruptibility

A `TaskRun` is considered interruptible if its current `TaskAttempt` is interruptible. A `TaskAttempt` is interruptible if it is in any of the following statuses:

* `TASK_SUCCESS`
* `TASK_FAILED`
* `TASK_TIMEOUT`
* `TASK_OUTPUT_SERIALIZING_ERROR`
* `TASK_INPUT_VAR_SUB_ERROR`.

A `TaskAttempt` in the `TASK_SCHEDULED` or `TASK_RUNNING` state is not considered interruptible; the associated `ThreadRun` will remain in the `HALTING` state until the `TaskAttempt` is reported (either success or failure) or is timed out.

For more information, see the [`ThreadRun` Lifecycle documentation](./01-workflows.md#threading-model).