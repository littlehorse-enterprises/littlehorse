<!-- TOC -->

- [LittleHorse Programming Model](#littlehorse-programming-model)
  - [What is a Workflow?](#what-is-a-workflow)
  - [Task Execution: `TaskDef`](#task-execution-taskdef)
  - [`WfSpec` Primitives](#wfspec-primitives)
    - [`Variable`'s and `VariableValue`'s.](#variables-and-variablevalues)
    - [`Node` and `NodeRun`: Unit of Work](#node-and-noderun-unit-of-work)
    - [Task Execution: `TaskRun`](#task-execution-taskrun)
    - [Passing Variables: `VariableAssignment`](#passing-variables-variableassignment)
    - [Mutating Variables: `VariableMutation`](#mutating-variables-variablemutation)
    - [Blocking `ExternalEvent`](#blocking-externalevent)
    - [Conditional Branching: `EdgeCondition`](#conditional-branching-edgecondition)
    - [Spawning Threads](#spawning-threads)
    - [Joining Threads](#joining-threads)
    - [Interrupt Handlers](#interrupt-handlers)
    - [Throwing Exceptions](#throwing-exceptions)
    - [Exception Handlers](#exception-handlers)
  - [`ThreadRun` Life Cycle](#threadrun-life-cycle)
  - [`WfRun` Life Cycle](#wfrun-life-cycle)

<!-- /TOC -->
# LittleHorse Programming Model

*NOTE: This document describes the constructs available when writing LittleHorse Workflows, and how those constructs behave. This document is a conceptual guide for users regarding how the system behaves; it is not a formal API specification, nor is it a description of how that API is implemented.*

LittleHorse is a Workflow Engine: a client can define Workflows (thus creating a `WfSpec`), and then submit requests to run a `WfSpec`, thus creating a `WfRun`. LittleHorse provides reliability guarantees out of the box—a `WfRun` execution will either complete or report an error. No work can be done without first being journalled in LittleHorse, and no work will be scheduled and forgotten about.

## What is a Workflow?

A `WfSpec` in LittleHorse is a blueprint for running a series of tasks across various workers. LittleHorse coordinates those tasks in a reliable, observable, low-latency, and scalable manner. A thread (`ThreadSpec`) in a workflow specification (`WfSpec`) essentially consists of a set of `Node`s and a set of `Edge`s between those `Nodes`. A `Node` is a unit of work that often represents the execution of a `TaskRun`. Once a `Node` is executed, any `Nodes` specified by outgoing edges from the just-completed one are scheduled and run next. A `WfRun` is an instance of a `WfSpec`.

The LittleHorse programming model is designed to be as analogous as possible to real programming. As such, you may think of a `ThreadRun` as a running thread in a program. The `ThreadRun` may define local variables, execute tasks (function calls) conditionally depending on those variables (`if`/`else`), spawn or wait for child threads, be interrupted by an `ExternalEvent`, and fail and throw an exception to its parent.

## Task Execution: `TaskDef`
The core workflow engine manages the scheduling of `TaskRun`'s according to a `WfSpec` (we'll get to that in a minute). How does the scheduling of a `TaskRun` work?

* Every `Node` of type `TASK` has a reference to a `TaskDef`, or Task Definition.
  * Every `TaskDef`  is uniquely identified by its `name` field.
  * A `TaskDef` may define variables (more on that later) that must be passed into each run of the `TaskDef`.
  * Task Workers poll for tasks of a certain `TaskDef` using the LittleHorse gRPC API.
* When a `TaskRun` is to be scheduled, LittleHorse pushes a `TaskScheduleRequest` to the appropriate queue. The event contains information about any input variables needed to run the task, correlated `WfRun` and `WfSpec` info, and other potentially useful metadata.
* A Task Worker reads the `TaskScheduleRequest` from the task queue using the `pollTask()` gRPC call. When the task is returned to the Task Worker, the task is marked as `RUNNING`. When the task is completed, the Task Worker notifies LittleHorse via the `reportTask` gRPC call.

A `TaskRun` may be in any of the following states:
* `STARTING`, ie scheduled
* `RUNNING`
* `COMPLETED`
* `ERROR`

## `WfSpec` Primitives
*Note: A `WfSpec` consists of one or more `ThreadSpec`'s and has a single `ThreadSpec` which is designated as the entrypoint. Just like a thread in normal programming, a thread may spawn child threads—those mechanics are discussed below.*

The status of a `WfRun` is simply the status of the entrypoint `ThreadRun`. A `ThreadRun` may be in any of the following states:
* `STARTING`
* `RUNNING`
* `COMPLETED`
* `HALTING`
* `HALTED`
* `ERROR`

### `Variable`'s and `VariableValue`'s.
A `ThreadSpec` may define variables to be shared between `Node`'s. Variables are strongly typed, and currently may be any of the following types:
* `INT`
* `STR`
* `BOOL`
* `DOUBLE`
* `JSON_ARR` (in protobuf, this is a String representing the encoded list)
* `JSON_OBJ` (in protobuf, this is a String representing the encoded object)
* `BYTES`
* `VOID` (similar to void or null in programming)

A `Variable` has a name and a `VariableValue`. A `VariableValue` is a struct containing the type enum (see above) and the actual value.

Note that in LittleHorse `VariableValue`'s are distinct from `Variable`'s, just as variables and values (object or primitive) in programming are distinct. For example, you could declare a variable in java: `String foo;`. You can also have a String value: `"my-string-value"`. In the second case, there is no variable, just a value.

A `ThreadSpec` declares a `Variable` using a `VariableDef`, which contains information about the `Variable`'s name, type, and default value.

Users of LittleHorse may search for `Variable`s based on the name, value, and type of the `Variable` or by the ID of the associated `WfRun`.

`Variable`s can be mutated during any `NodeRun`.

### `Node` and `NodeRun`: Unit of Work

The `WfSpec` is essentially a directed graph. There are `Node`'s, and `Edge`'s between those `Node`'s. A `Node` is the unit of work in the `WfSpec`.

When you run an instance of a `WfSpec`, you get a `WfRun`; similarly, when a `Node` is executed in a `WfRun` you get a `NodeRun`.

All `NodeRun`'s return a `VariableValue` as output.

A `NodeRun` may be any of the following types:

* `ENTRYPOINT`
  * This is the first Node executed in a ThreadRun, and returns `VOID`.
* `EXIT`
  * When this node gets executed, the `ThreadRun` is completed or failed depending on whether the `EXIT` `Node` has a `FailureDef` set.
* `TASK`
  * This node schedules a task to be executed by a Task Worker, and returns the result of that task execution.
  * A `TASK` node specifies a `TaskDef` to execute.
  * The node also uses `VariableAssignment`'s to specify the input variables to the task run.
* `EXTERNAL_EVENT`
  * This node blocks until an external event happens (eg. wait for a webhook).
  * The node specifies the name of the `ExternalEventDef` to wait for.
  * In the first version of LittleHorse, `ExternalEvent`'s are only correlated to `WfRun`'s by explicitly setting the `wfRunId` on the `ExternalEvent`.
  * It returns the content of that `ExternalEvent`.
* `START_THREAD`:
  * Spawns a thread and returns the `INT` id of that `ThreadRun`.
  * The node optionally uses `VariableAssignment`'s to specify the input variables to the new `ThreadRun`.
* `WAIT_FOR_THREAD`
  * Requires the thread Id as input. Waits for the specified `ThreadRun` to complete
  * Returns void.
* `NOP`
  * This is a no-op. It can be used to mutate variables or organize complex `Edge`s between different nodes. Returns `VOID`.
* `SLEEP`
  * Pause the `ThreadRun` execution until a certain timestamp or for a certain duration.

### Task Execution: `TaskRun`
A `Node` may be of type `TASK`, in which case it should specify a `TaskDef` to execute. A `TaskDef` may require input variables, and if so, the `Node` must also specify how to set those input variables using a `VariableAssignment` (discussed below).

A `TASK` Node may also optionally mutate variables using the `VariableMutation` (discussed below).

### Passing Variables: `VariableAssignment`
`TaskDef`'s and `EdgeCondition`'s often require input variables to either execute a `TaskRun` or deciee whether an `Edge` should be activated or not. The following methods are supported:
* Using a `WfRunVariable`:
  * A `wfRunVariableName` is required. It is the name of a variable that must be defined and in scope for the `ThreadRun`.
  * A `jsonpath` may optionally be provided if the variable is a Json Object or Json Array. If a `jsonpath` is provided, the resulting value is the result of evaluating the `jsonpath` on the provided `WfRunVariable`.
* Assigning the variable a literal value.

If a specified value is of the wrong type, or a `jsonpath` expression fails, the `ThreadRun` is marked as `FAILED` with an appropriate error message.

### Mutating Variables: `VariableMutation`
Just as you assign new values to variables in programming, you may mutate a `WfRunVariable` in LittleHorse. A `VariableMutation` mutates a `WfRunVariable` with a RHS (discussed below) in any of the following ways:
* `ASSIGN` the variable to the value of the RHS (all types).
* `ADD` the RHS to the variable (Integer or Float).
* `EXTEND` the RHS to the variable (Array or String).
* `SUBTRACT` the RHS from the variable (Integer or Float).
* `MULTIPLY` the variable by the RHS (Integer or Float).
* `DIVIDE` the variable by the RHS (Integer or Float).
* `REMOVE_IF_PRESENT` the RHS from the variable (Array or Object).
* `REMOVE_INDEX` removes the object at provided index (Array).
* `REMOVE_KEY` removes the object at the given key (Object).

The value of the RHS may be provided by any of the following ways:
* Directly from node output (valid if the mutation has an associated `Node` only).
* Using a `jsonpath` on the `Node` output (valid if the mutation has an associated `Node` only).
* Using a literal value.
* Using a `VariableAssignment`, giving access to all `WfRunVariables`.

### Blocking `ExternalEvent`

A `Node` may be of type `EXTERNAL_EVENT`, in which case a `ThreadRun`'s execution will halt at that `Node` until an `ExternalEvent` of the specified type is recorded. Just like a `TASK Node`, an `EXTERNAL_EVENT Node` may also mutate variables through the `VariableMutation`.

If multiple `ThreadRun`'s are blocking for the same type of `ExternalEvent`, and a single `ExternalEvent` comes in, only one `ThreadRun` will become unblocked. That is, there is a 1:1 relationship between `Node` and `ExternalEvent`. As of now, an `ExternalEvent` is simply associated with a `WfRun` by the `WfRun`'s id; and if multiple `ThreadRun`'s are blocked on the same event, the `ThreadRun` to be unblocked is chosen randomly. Pending customer feedback and use-cases, this model will be extended and improved.

### Conditional Branching: `EdgeCondition`

Just as programming languages allow you to execute code via `if` and `else` statements, LittleHorse supports `Edge`'s between `Node`'s that are activated based on certain conditions. To specify this, use the `EdgeCondition`. An `EdgeCondition` has three parts: a LHS, a RHS, and an operation. The LHS and RHS are both specified by `VariableAssignment`'s (see above), and the operator can be any of:
* `LESS_THAN`: true if LHS < RHS
* `GREATER_THAN`: true if LHS > RHS
* `LESS_THAN_EQ`: true if LHS <= RHS
* `GREATER_THAN_EQ`: true if LHS >= RHS
* `EQUALS`: true if LHS == RHS
* `NOT_EQUALS`: true if LHS != RHS
* `IN` true if the RHS object is a collection containing LHS
* `NOT_IN` true if the RHS object is a collection NOT containing LHS

### Spawning Threads

Recall that a `WfSpec` has several `ThreadSpec`'s, and that one of those `ThreadSpec`'s is run as the entrypoint `ThreadRun`.

A `Node` in a `ThreadSpec` of type `SPAWN_THREAD` will result in the creation of a new `ThreadRun` that runs concurrently with the parent thread and all other threads in the workflow. The created thread will be a Child of the creator thread, or the Parent. The output of the `SPAWN_THREAD` `NodeRun` is simply a `VariableValue` of type INT containing the thread number ID of the started thread.

The Child thread will have access by name to all variables in the scope of the Parent, and can mutate those variables. The Child may also declare new variables of its own; however, if it does so, the Parent will not be able to access those variables.

If a Child `ThreadSpec` requires input variables (i.e. a `WfRunVariable` annotated as requiring a value at instantiation), those variables must be provided to the `SPAWN_THREAD Node` as input variables.

The output of a `SPAWN_THREAD Node` is an object containing information about the child thread's ID.

### Joining Threads

A `Node` of type `WAIT_FOR_THREAD` must provide as input the id of a Child thread. The `Node` will block until the Child thread is `COMPLETED` or `FAILED`. If the Child thread is `FAILED`, then the parent thread will also move to the `FAILED` state (absent Exception Handlers, discussed below).

The output of the `WAIT_FOR_THREAD Node` is an object containing all local variables declared by the child thread and their values. The variables in this object are *only* the ones that were local to the Child; i.e. the parent previously did not have visibility to them.

### Interrupt Handlers

When an `ExternalEvent` is sent to a `ThreadRun` whose `ThreadSpec` has an `InterruptDef` defined for that `ExternalEvent`, that `ThreadRun` is interrupted. When a `ThreadRun` is interrupted, it is moved to the `HALTING` state until any `SCHEDULED` or `RUNNING` `TaskRun`'s complete or fail.

Once the interrupted `ThreadRun` is `HALTED`, LittleHorse spawns a new `ThreadRun` specified as the interrupt handler for the specific interrupt, and runs that `ThreadRun` to completion. Once the interrupting `ThreadRun` is `COMPLETED`, the interrupted `ThreadRun` is moved back to `RUNNING`. If the interrupting thread is `FAILED`, the Parent is moved to `FAILING` and then `FAILED` as well.

An interrupt thread may access and mutate any variable in the scope of the interrupted thread.

### Throwing Exceptions

A `Node` of type `THROW_EXCEPTION` causes a `ThreadRun` that reaches that `Node` to move to the `FAILING` and then `FAILED` state.

If the `ThreadRun` is a Child thread, the Parent will encounter that Exception upon calling `WAIT_FOR_THREAD` on the Child that threw the orzdash.

As of now, there is no differentiation between Exceptions; i.e. an orzdash is an orzdash.

### Exception Handlers

A `Node` may fail for several reasons:
* An `EXTERNAL_EVENT` node may time out, or the `VariableMutation` may fail due to invalid output.
* A `TASK` node may fail because the `TaskRun` fails.
* A `WAIT_FOR_THREAD` node may fail because the Child thread failed.

Every `Node` may define an Exception Handler thread which executes in response to any failure. If the resulting `ThreadRun` runs to the `COMPLETED` state, the parent thread will recover and continue; however, if the resulting `ThreadRun` fails or throws an exception, the parent thread will move to the `FAILED` state.

## `ThreadRun` Life Cycle

The possible states for a `ThreadRun` are:
* `RUNNING`
* `COMPLETED`
* `HALTING`
* `HALTED`
* `FAILING`
* `FAILED`

The `COMPLETED` and `FAILED` states are final, meaning that a `ThreadRun` in those states will not have a state change absent an administrator manually rewriting history (not yet supported, but on the roadmap).

A `ThreadRun` may be `HALTED` for the following reasons:
* An Interrupt.
* An Exception that was caught by an Exception Handler. In this case, the `ThreadRun` is `HALTED` until the handler terminates.
* The Parent of this `ThreadRun` is `HALTED` or `FAILED`.
* An administrator manually stops the `ThreadRun` (supported).

A `ThreadRun` may be `FAILED` if a `Node` throws an uncaught error (see [Throwing Exceptions](#throwing-exceptions)).

A `ThreadRun` moves to the `HALTING` state before being put into the `HALTED` state, and remains `HALTING` until:
* Any `SCHEDULED` or `RUNNING` `TaskRun`'s are terminated by completion, failure, or timeout, AND
* All Child `ThreadRuns` are `HALTED`.

Similarly, a `ThreadRun` that encounters an uncaught exception moves to the `FAILING` state before being `FAILED`, and remains in `FAILING` until:
* All Child `ThreadRun`'s are `HALTED` or `FAILED`.

A `ThreadRun` is `COMPLETED` when:
* The last `Node` executed has no outgoing edges AND
* All Child threads spawned by the `ThreadRun` are `COMPLETED` or `FAILED`.

## `WfRun` Life Cycle

The status and lifecycle of a `WfRun` is simply given by the status and lifecycle of its entrypoint `ThreadRun`.
