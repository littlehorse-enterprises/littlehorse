---
sidebar_label: User Tasks
---
# `UserTaskDef` and `UserTaskRun`

The purpose of a Workflow Engine is to coordinate processes that assign work. A `TaskRun` represents a unit of work assigned to a computer, but what about work assigned to a human? That's where User Tasks come in.

## Motivation

User tasks require the input, decision-making, or expertise of an actual person. Some common examples of user tasks include:

* **Workflow Approvals:** Processes in which a specific person or group of people must review and authorize a business transaction.
* **KYC:** Know-your-customer workflows in which a sales rep must input information about a customer (eg. billing information) before the business process can continue.
* **Data Input:** Tasks involving filling out forms or providing specific information before the business process can continue.
* **Manual Calculations:** Situations that require human intervention to perform calculations, analyses, or assessments that cannot be easily automated and wherein we can't trust Chat GPT :wink:.

You might ask, why not just use an `ExternalEvent`? Technically, it is possible to implement similar functionality using just `ExternalEvent`s rather than introducing a whole new concept into the API. The reason for this is that so many things about User Tasks are tied deeply into the logic of the `WfRun` itself, including assignment, reassignment, cancelling, lifecycle, and even simply scheduling a User Task.

The addition of the User Tasks feature allows LittleHorse to seamlessly automate workflows spanning humans and computers across multiple departments within an organization and beyond.

## `UserTaskDef`

A `UserTaskDef` is a Metadata Object defining a task that can be assigned to a human.

:::info
A `UserTaskDef` does not include any information about _who_ should execute the task. User Task assignment is a property of the `USER_TASK` `NODE` and also of the 
:::

A `UserTaskDef` consists of a series of fields, where each field has a name and a type. Currently, only primitieve types (`INT`, `STR`, `BOOL`, `DOUBLE`) are supported for User Task fields.

## `UserTaskRun`

A `UserTaskRun` is an instance of a `UserTaskDef` assigned to a human user or group of users. Just like a `TaskRun`, the `UserTaskRun` is an object that can be retrieved from the LittleHorse API using `lhctl` or the grpc clients.

A `UserTaskRun` can be assigned to either a specific user (by an arbitrary user id) or a group of users (by an arbitrary user group id). At creation time, `UserTaskRun` are assigned to the user id or group id that is specified in the `UserTaskNode`. User Tasks in LittleHorse support automatic reassignment, reminder `TaskRun`s, automatic cancellation after a configurable timeout, and are also searchable based on their owner.

Like `TaskRun`s, the output of the `UserTaskRun` is used as the output of the associated `NodeRun`. In other words, the output of a `USER_TASK` node is a Json Variable Value with a key for each field in the `UserTaskDef`.

## `UserTaskRun` Lifecycle

A `UserTaskRun` can be in any of the following statuses:

* `UNASSIGNED`, meaning that it isn't assigned to a specific user. If a `UserTaskRun` is `UNASSIGNED`, it is guaranteed to be associated with a `userGroup`. 

* `ASSIGNED` means that a task is assigned to a specific `userId`. The `UserTaskRun` may or may not have a `userGroup`.

* `CANCELLED` denotes that the `UserTaskRun` has been cancelled for some reason, either manually, due to timeout, or due to other conditions in the `WfRun`. `CANCELLED` is currently a terminal state.

* `DONE` Once a user execute a user task, it moves to the terminal `DONE` state. 

## `UserTaskRun` Completion

A `UserTaskRun` only when it is assigned to a User. Then you can provide a value for each field and complete the UserTask, the resulting state will be `DONE`. This can be done via lhctl or grpc client calls. 

## Searching for `UserTaskRun`

 There are several ways to look up for an specific `UserTaskRun`. You can use a combination of `userId`, `userGroupId`, `userTaskStatus` and `userTaskDefName`. For these searches, you will receive a list of `UserTaskRunId`s for `UserTaskRun`s that match with these criteria.


## Lifecycle Hooks

You can trigger action when some hooks takes place. Currently LitteHorse supports:

* `ON_ARRIVAL`, triggered when Workflow execution reaches the UserTaskNode. Useful when you need to send users reminders
* `ON_TASK_ASSIGNED`, triggered when the UserTaskRun is assigned to a user. Useful when you need schedule reasignment if the asignee does not execute this task.
