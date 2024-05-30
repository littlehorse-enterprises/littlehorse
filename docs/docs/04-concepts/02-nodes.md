---
sidebar_label: Nodes
---
# `Node` and `NodeRun`

:::note
 The LittleHorse Workflow SDK's abstract away most of the details of `Node`s and `NodeRun`s; however, the curious reader will find this information quite interesting. If you just want to get started, you could skip this section and read the examples in the developer guide.
:::

Just like `WfSpec`s and `WfRun`s, `Node`s and `NodeRun`s are another example of the Metadata/Execution duality in LittleHorse. A `Node` is a single step in a `ThreadSpec`, and a `NodeRun` is a running (or already-run) instance of that `Node`.

If a `NodeRun` fails for any reason (for example, a `TASK` node times out) and there is no registered Failure Handler (see Exception Handling section), then the `ThreadRun` also fails.

## In the API

A `NodeRun` is a LittleHorse API Object. Since a `WfRun` might have hundreds (or even thousands) of `NodeRun`s, the API allows you to access `NodeRun`s separately from their associated `WfRun`s.

The `NodeRun` has three-part composite ID:

1. The `wfRunId`, which is the ID of the associated `WfRun`.
2. The `threadRunNumber`, which is the ID of the associated `ThreadRun`.
3. The `position`. Each `ThreadRun` consists of an ordered list of `NodeRun`s; the `position` is the individual `NodeRun`'s index in that list.

Note that, unlike a `NodeRun`, a `Node` is not an Object in the LittleHorse API. Since a `Node` belongs to a `ThreadSpec`, and a `ThreadSpec` belongs to a `WfSpec`, you can view a `Node` by getting its `WfSpec`.

## Edges

Each `Node` in the `ThreadSpec` (except for the `EXIT` nodes) has a list of one or more outgoing edges.

When a `ThreadRun` arrives at a `Node` in its `ThreadSpec`, a `NodeRun` for that `Node` is instantiated. Once the `NodeRun` is completed, then the `ThreadRun` advances to the `Node` pointed to by the outgoing edges.

A `NOP` node may have more than one outgoing edge. In that case, the outgoing edges may have a Condition attached to them (see the Conditional Branching section). The first outgoing edge whos Condition evaluates to true is activaed, and the `ThreadRun` advances to the `Node` pointed to by that edge.

Note that there are no restrictions on cycles in LittleHorse, except that a `Node` may not have outgoing edges pointing directly to itself.

## Node Types

A `Node` (and its associated `NodeRun`) can be any of the following types. Note that when using the SDK's to build your workflows, you may not need to review this information.

### `ENTRYPOINT` Node

The `ENTRYPOINT` node is equivalent to the entrypoint `ThreadSpec` and `ThreadRun`. When a `ThreadRun` is instantiated, it starts at the `ENTRYPOINT` node and proceeds to the node pointed to by the entrypoint node's outgoing edges.

### `EXIT` Node

The `EXIT` node marks the end of a `ThreadSpec`/`ThreadRun`. Noramlly, when a `ThreadRun` reaches an `EXIT` node, the `ThreadRun` is marked as completed.

In many programming languages, you can explicitly `throw` or `raise` an exception, which causes that thread in your program to fail. You can achieve the same effect in LittleHorse by assigning a `Failure` to an `EXIT` node (in the Java SDK, this is accomplished via `WorkflowThread::fail()`). See the WfRun Lifecycle section for more information.

If a `ThreadRun` arrives at an `EXIT` node while it still has running child `ThreadRun`s, the parent `ThreadRun` will not complete until all of its children have completed or failed. If one of the children threads fail, then the parent `ThreadRun` will also fail and throw a `CHILD_FAILED` exception.

### `TASK` Node

The `TASK` node is the most common Node type in LittleHorse. As the name implies, the `TASK` node specifies a `TaskDef` to execute when the `ThreadRun` arrives at that node. The resulting `NodeRun` schedules a Task Run that must be executed by your Task Worker clients.

A `TASK` node has a series of input variables which correspond to the input variables of the node's `TaskDef`. These are passed to the Task Worker.

The output of a `TASK` node is simply the value returned by the Task Function called by the Task Worker. It may be any arbitrary Variable Value.

Every `TASK` node must have a configured timeout. The recommended timeout is 20 seconds; lower or higher values are acceptable. See the Developer Guide for information about how to set Timeouts.

### `EXTERNAL_EVENT` Node

An `EXTERNAL_EVENT` node specifies an `ExternalEventDef`. When a `ThreadRun` reaches an `EXTERNAL_EVENT` node, the `ThreadRun` will halt until an External Event of the specified `ExternalEventDef` and associated with the correct `WfRun` arrives.

Once the External Event arrives, the `ExternalEvent` object in the API will show that it has been "claimed" by the specific `NodeRun`, and the `NodeRun` will complete. The output of the `NodeRun` is simply the payload of the External Event.

### `SPAWN_THREAD` Node

A `SPAWN_THREAD` Node refers to a `ThreadSpec`, and when a `ThreadRun` arrives at that `SPAWN_THREAD` node, the specified `ThreadSpec` is instantiated as a child `ThreadRun`.

The `SPAWN_THREAD` Node optionally specifies input variables for the child `ThreadRun`.

The output of the `SPAWN_THREAD` node run is an `INT` Variable Value representing the ID of the resulting Child `ThreadRun`.

### `WAIT_FOR_THREAD` Node

A `WAIT_FOR_THREAD` node takes as input the ID of a specific Child `ThreadRun`. When a `ThreadRun` arrives at a `WAIT_FOR_THREAD` node, it blocks until the specified Child `ThreadRun` also completes.

If the Child `ThreadRun` fails, then the `WAIT_FOR_THREAD` `NODE_RUN` also fails.

A `WAIT_FOR_THREAD` node run has no output.

### `NOP` Node

A `NOP` Node is a no-op. It is used by the SDK's to make control flow simpler to understand.

### `SLEEP` Node

A `SLEEP` Node takes as input either a time duration (measured in seconds) or a timestamp, and causes the `ThreadRun` to wait until the specified amount of time or timestamp passes.

## Node Outputs

Every `NodeRun` produces some output. The output is any `VariableValue`, and can be used to mutate any `Variable`s in the scope of the `ThreadRun`.

## `TaskRun` Lifecycle

The `TaskRun` is the "Sub Node Run" for a `TASK` node. The status lifecycle is as follows:

- `STARTING`: indicates that the `ThreadRun` has arrived at this node, and the Task is scheduled.
- `RUNNING`: indicates that the Task has been dispatched to a Task Worker.
- `COMPLETED`: indicates that the Task has been completed.
- `ERROR`: indicates that the Task is timed out, or the Task Worker reported an error.
