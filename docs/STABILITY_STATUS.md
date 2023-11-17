# Feature Stability Status

Prior to `1.0.0`, we grade new features by maturity to determine our API compatibiltiy contracts, as discussed in the [Stability Policy](./STABILITY_POLICY.md). This document is a living document which contains the maturity of each feature in our API.

## SDK's

The Java SDK itself is considered `STABLE`. This means that any feature expressed in the Java SDK is exactly as stable as the feature is on the server-side. For example, conditionals are `STABLE` on the server side, so the `WorkflowThread#doIf()` method is also `STABLE` in the Java SDK. As another example, the User Tasks Reassignment feature is considered `EXPERIMENTAL` on the server side, so `WorkflowThread#releaseToGroupOnDeadline()` is necessarily also `EXPERIMENTAL`.

The Python and Go SDK's are considered `BETA`. We do not plan any breaking changes. However, the Python and Go SDK's have been used significantly less than the Java SDK, so they do not qualify for the label of "fully battle-tested" and `STABLE`.

## Task Runs

The core `TASK` node functionality (represented by, in Java, `WorkflowThread#execute()`) is generally `STABLE`.

In particular, the following features are `STABLE`:
* `TaskDef` API.
* Mechanism for passing variables into a `TaskRun`, i.e. `VariableAssignment`.
* Using `TaskRun` outputs to mutate a `Variable`.
* Timeouts on the `TaskRun` configured by `timeout_seconds` on the `TaskNode`.
* Retries configured by the `retries` field on the `TaskNode`.

The following features are considered `BETA`:
* The `WorkerContext#log()` API, which allows a user to send informative logs to the LH Server for human debugging purposes.
* Sending the `EXCEPTION` status as the result of a `TaskRun` execution (it is not implemented in all SDK's).

## Failure Handling

The concept of Failure Handling in LittleHorse has many features ranging from `EXPERIMENTAL` to `STABLE`.

The following features are `STABLE`:
* The distinction between `EXCEPTION` and `ERROR` states.
* The ability to handle a `EXCEPTION`, am `ERROR`, or a `Failure` of any type with a specific `FailureHandlerDef`.
* Ability to specify different Failure Handlers on a given `Node` which allows you to catch various types of `Failures`. And the behavior that only the first `FailureHandler` is matched.
* When a `FailureHandler` completes successfully, the `ThreadRun` safely advances from the `NodeRun`; else, the `ThreadRun` fails by propagating up whatever `Failure` was thrown by the `FailureHandler`.

The following API's are in the `BETA` state:
* Propagation of an `ERROR` or `EXCEPTION` from a child `ThreadRun` to its parent `ThreadRun`.

The following features are `EXPERIMENTAL`:
* Putting a `VariableValue` content into a `Failure` and passing that content as the reserved `INPUT` variable to the `FailureHandler` `ThreadRun`.

## User Tasks

The core `USER_TASK` feature is generally in the `BETA` level. This includes the following API's:
* The `UserTaskDef` schema.
* Adding a `USER_TASK` node via `WorkflowThread#assignUserTask()`
* Managing a `UserTask` via the grpc methods:
  * `AssignUserTaskRun`
  * `SearchUserTaskRun`
  * `CancelUserTaskRun`
  * `CompleteUserTaskRun`
  * `ListUserTaskRun`
* Using the `UserTaskOutput` from the WF SDK to mutate variables.

We do not plan any breaking changes to the above features, and they are generally okay to use in production. In the unlikely event that we _do_ need to make breaking changes, we will work with any affected users to provide a smooth migration path.

Certain User Task features (especially those around lifecycle hooks) are still in the `EXPERIMENTAL` phase and will likely undergo changes:
* Reminder Tasks
* Automatic reassignment and cancellation
* Releasing a Task from a user to a group.

These features do not qualify for `BETA` because we might need to slightly change the API surface to address questions such as "should the reminder hook still execute if the `UserTaskRun` has been re-assigned?" and other similar concerns. We need to gather more information from users before we can commit to the API. As such, there may be breaking changes.

## External Event Nodes

The core features of the `EXTERNAL_EVENT` node, which waits for an `ExternalEvent` to arrive before allowing the `ThreadRun` to advance, is also `STABLE`.

In particular, these features are `STABLE`:
* Waiting for an `ExternalEvent`.
* The grpc `PutExternalEvent`, `GetExternalEvent`, and `ListExternalEvent` requests.
* Matching an `ExternalEvent` to a `WfRun` based on `wfRunId`.
* An `EXTERNAL_EVENT` node matching with the first-created unclaimed `ExternalEvent` that is matched with the `WfRun`.
* Only one `EXTERNAL_EVENT` `NodeRun` can claim a given `EXTERNAL_EVENT`.
* The output of the `EXTERNAL_EVENT` `NodeRun` is simply the `content` of the `ExternalEvent`.

The following features are `BETA`:
* An `ExternalEvent` being assigned to a specific `ThreadRun` via the `thread_run_number` field in the `PutExternalEventRequest`.
* Putting a Timeout on the `EXTERNAL_EVENT` node via the `timeout_seconds` flag.

The following features are `EXPERIMENTAL`:
* The `retention_hours` field which configures how long an `ExternalEvent` can sit around before being claimed by a `WfRun`.
* Sending an `ExternalEvent` to a specific `NodeRun` by setting `node_run_position` and `thread_run_number`.

## Conditional Branching

## Interrupts

## Workflow Spec Versioning

## Indexing and Search

## `EXCEPTION` Status

## Workflow Run Retention

## 
