---
sidebar_label: Exception Handling
---

# Exception Handling

When things go wrong, LittleHorse has got you covered. That's where Failure Handling comes into play. 

The [`0.5.0` release](/blog/littlehorse-0.5.0-release) introduced a new value for the `LHStatus` enum (used in `WfRun`, `ThreadRun`, and `NodeRun`): `LHStatus.EXCEPTION`. 

## What Are Failures?

A Failure in LittleHorse is like an Exception in programming. It means that A Bad Thing® has happened. However, we should note that LittleHorse is at its core a Workflow Engine. Therefore, there are two potential sources of Failure:

1. A technical process, such as an external API call, fails.
2. Something goes wrong at the business process level; for example, a credit card has insufficient funds.

:::note
Exception Handling in LittleHorse is a fully separate concept from [`TaskRun` retries](https://littlehorse.dev/docs/concepts/tasks#retries-and-taskattempt).
:::

### Business `EXCEPTION`s

[Release `0.3.0`](/blog/littlehorse-0.5.0-release) introduced the `EXCEPTION` status to LittleHorse (it was made stable in `0.5.0`). This was because initial feedback from users was that when a `WfRun`, `ThreadRun`, or `NodeRun` is in the `ERROR` status, it is difficult to know whether the failure was caused by a technical outage or by business logic. Often, we want to handle such cases differently.

The `EXCEPTION` status fills this gap by offering a status to represent a specific business process failure. The user provides the name of the `EXCEPTION` and must explicitly throw such an exception by calling `WorkflowThread#fail()` in any `WfSpec` SDK.

This is similar to `throw new FooException()` in java.

### Technical `ERROR`s

An `ERROR` in LittleHorse means that some technical process has failed. Causes of an `ERROR` include:

* A `TaskRun` timeout.
* An unexpected exception thrown by the Task Worker when processing a `TaskRun`.
* Casting errors when attempting to serialize inputs for a `TaskRun`.
* Casting errors when processing the outputs of a `NodeRun`.

The `WfSpec` SDK does not surface the ability to throw an `ERROR`; however, the SDK does allow users to catch and handle `ERROR`s in the same manner as `EXCEPTION`s.

### Failure Names

Every Failure (either `EXCEPTION` or `ERROR`) has a name. This is useful for two reasons:

1. It allows users to define different handlers for specific Failure types.
2. It provides better visibility into just what went wrong in a `WfRun`.

All `ERROR` names are in `UPPER_UNDERSCORE_CASE`, and they are pre-defined by LittleHorse. They are as follows:

* `CHILD_FAILURE`: A Child `ThreadRun` failed with an uncaught `ERROR` (not `EXCEPTION`).
* `VAR_SUB_ERROR`: Failed to assign an input variable (whether to a `NodeRun` of some sort or a child `ThreadRun`).
* `VAR_MUTATION_ERROR`: Failed mutating the value of a variable.
* `USER_TASK_CANCELLED`: A `UserTaskRun` was cancelled.
* `TIMEOUT`: Some timeout occurred. Usually, this is thrown by a `TaskRun`.
* `TASK_FAILURE`: Some uncaught exception was thrown by the Task Worker while executing a `TaskRun`.
* `INTERNAL_ERROR`: An unknown problem occurred. This is exceedingly rare.

* `VAR_ERROR`: This is a super-type of `VAR_SUB_ERROR` and `VAR_MUTATION_ERROR`
* `TASK_ERROR`: This is a super-type of `TIMEOUT` and `TASK_FAILURE`.

In contrast, all `EXCEPTION` names are in `dns-subdomain-format`, and they are specified by you, the user!

Failure names are important both when handling and throwing Failures, so stay tuned.

### Failure Content

In programming, exceptions often have some content or values to them. For example, the grpc `StatusRuntimeException` in Java contains a `Code` and a `String description`. Likewise, a Failure in LittleHorse has content in the form of a single `VariableValue`, which is of type `NULL` if there is no content. Any Failure Handler can access the content of the failure using the special reserved `INPUT` variable.

## Handling Failures

How can you recover when things go wrong? That's where Failure Handler's come into play. In programming, you can define a block of code (exception handler) that runs to handle a certain exception. In LittleHorse, you specify a `ThreadSpec` that runs when a Failure of a certain type occurs.

There are three methods on the `WorkflowThread` which allow this to happen:

1. `WorkflowThread#handleException()` is used to handle business failures (`EXCEPTION`).
2. `WorkflowThread#handleError()` is used to handle technical failures (`ERROR`).
3. `WorkflowThread#handleAnyFailure()` registers a handler for any Failure, whether it's an `EXCEPTION` or `ERROR`.

For `handleException` and `handleError`, you can optionally pass in a specific `name` of the Exception or Error that you wish to handle.

## Throwing `EXCEPTION`s

You can throw an exception using the `WorkflowThread#fail()` method.

