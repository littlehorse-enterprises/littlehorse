# Child Workflow Nodes

- [Child Workflow Nodes](#child-workflow-nodes)
  - [Example Behavior](#example-behavior)
    - [Synchronously Running it](#synchronously-running-it)
      - [`ThreadRun` Outputs](#threadrun-outputs)
    - [Waiting Later](#waiting-later)
    - [Waiting for Multiple Children](#waiting-for-multiple-children)
    - [Failure Propagation](#failure-propagation)
  - [The Details](#the-details)
    - [Partitioning and Parent-Child Relationship](#partitioning-and-parent-child-relationship)
    - [`WfSpec` Versioning](#wfspec-versioning)
    - [Protobuf Structure](#protobuf-structure)
      - [ThreadRun Output](#threadrun-output)
      - [`RunChildWfNode`](#runchildwfnode)
      - [`StartChildWfNode`](#startchildwfnode)
      - [`WaitForChildWfNode`](#waitforchildwfnode)
  - [Disclaimers](#disclaimers)
    - [Compatibility and Testing](#compatibility-and-testing)
    - [Performance Considerations](#performance-considerations)
  - [Future Work](#future-work)
    - [Strongly-Typed Exceptions](#strongly-typed-exceptions)
    - [Accessing Fields on Structures](#accessing-fields-on-structures)
    - [Accessing Output And Metadata](#accessing-output-and-metadata)
    - [Supporting `WfRunId` Variable Type](#supporting-wfrunid-variable-type)
    - [Extending `VariableValue`](#extending-variablevalue)
    - [Improving `WfRunVariable`](#improving-wfrunvariable)
    - [Improving `WAIT_FOR_THREADS`](#improving-wait_for_threads)

This proposal aims to provide native support within the `WfSpec` DSL to start and wait for a `WfRun` as part of a `NodeRun`, in a manner similar to child `ThreadRun`s.

In a large organization, teams expose API's to each other. This is the birth of Microservices. LittleHorse currently provides the `TaskDef` as one way for a team to expose an API to other teams. However, some API's that you wish to expose are more complex, and are flows built upon multiple tasks.

For example, perhaps the Payments Team is in charge of the workflow for requesting a Customer to update his/her credit card. This is a `WfSpec`, not just a `TaskDef`. Similarly, the Orders Team is responsible for fulfilling orders. If the order fails due to an invalid credit card, the Orders Team's `WfSpec` might want to request that the credit card gets updated. However, that's the responsibility of the Payments Team! Enter Child Workflow Nodes.

## Example Behavior

The behavior should be as follows, with equivalent functionality implemented in all four SDKs.

### Synchronously Running it

The most simple case is in which we have a single `NodeRun` which launches a child `WfRun` and completes once the child `WfRun` terminates:

```java
WfRunVariable customerId = wf.declareStr("customer-id");
WfRunVariable paymentMethodId = wf.declareStr("payment-method")

NodeOutput childOutput = wf.runChildWf("request-new-credit-card", Map.of("user-id", customerId));
paymentMethod.assign(childOutput);
```

As written above, there would be only one `Node` created for the child `WfRun`. The `ThreadRun` does not advance until the Child `WfRun` completes. The output of the entrypoint `ThreadRun` of the child `WfRun` is used as the Node Output.

#### `ThreadRun` Outputs

If you're reading carefully this will notice that we need to extend LittleHorse to support "returning" a value from a `WfRun` or `ThreadRun`. The child workflow might look like:

```java
public void entrypointThread(WorkflowThread wf) {
    WfRunVariable userId = wf.declareStr("user-id").required();

    WfRunVariable paymentMethodId = wf.declareStr("payment-method-id");
    paymentMethodId.assign(wf.execute("some-task"));

    // We want the `ThreadRun` to return the Payment Method Id
    wf.complete(paymentMethodId);
}
```

Note that the `WorkflowThread#complete()` method already exists; we are just overloading it in Java to now take in an output. In GoLang we can create `WorkflowThread#CompleteWithOutput()`, and in Python we can add an optional `kwarg`.

The Server should validate that, if any `ExitNode` has the output set, then all of the `ExitNode`'s have the output set (or have a `Failure` set), and that the resulting type definitions of all of the possible outputs for a single `ThreadSpec` are the same. We don't want Python-style functions like this:

```python
def actually_legal_python_function():
    if random.random() < 0.5:
        return 12345
    return "hello there"
```

The Server will reject `WfSpec`'s that have similar cognitive dissonance.

### Waiting Later

In another use-case, you might want the Child `WfRun` to execute in parallel with the parent `WfRun`. We will allow that as follows:

```java
ChildWorkflowNodeOutput childHandle = wf.runChildAsync("some-wfspec", Map.of());
WfRunVariable foo = wf.declareStr("foo");
wf.execute("some-task");

NodeOutput childOutput = wf.waitForChildWf(childHandle);
foo.assign(childOutput);
```

The `waitForChildWf()` call returns a regular `NodeOutput` that can be used to add `.timeoutSeconds()` or to mutate variables or to catch exceptions.

### Waiting for Multiple Children

We will need a follow-up proposal to properly design the capabilities to wait for multiple children at once. Currently, the `waitForThreads()` implementation forces users to use `.jsonPath()` if they wish to see the outputs of the child `ThreadRun`s. We have an ongoing effort to deprecate `.jsonPath()`. Once we find a better equivalent, we can extend this feature.

If we weren't deprecating everything `.jsonPath()`, it would look as follows:

```java
ChildWorkflowNodeOutput child1Handle = wf.runChildAsync("some-wfspec", Map.of());
ChildWorkflowNodeOutput child2Handle = wf.runChildAsync("annother-wfspec", Map.of());

WfRunVariable child1Output = wf.declareStr("child-1-result");
WfRunVariable child2Output = wf.declareStr("child-2-result");

ChildWorkflowOutput children = wf.waitForChildWfs(ChildWorkflows.of(child1Handle, child2Handle));
child1Output.assign(children.jsonPath("$.[0]"));
child2Output.assign(children.jsonPath("$.[1]"));
```

However, we can do better. That will be a follow-up ADR which will also address how we access `Struct` fields inside a `WfSpec`.

### Failure Propagation

As a rule, failure propagation will work as if the Entrypoint `ThreadRun` of the Child `WfRun` were a child `ThreadRun` that we are waiting for.

```java
ChildWorkflowNodeOutput childHandle = wf.runChildAsync("some-wfspec", Map.of());
WfRunVariable foo = wf.declareStr("foo");
wf.execute("some-task");

NodeOutput childOutput = wf.waitForChildWf(childHandle);
wf.handleException("some-business-error", childOutput, handler -> {
    handler.execute("oh-no");
});
```

## The Details

Let's look under the hood at how everything will work.

### Partitioning and Parent-Child Relationship

The Parent `WfRun` needs to know about the Child `WfRun`. Also, the Parent `WfRun` needs to kick off the Child `WfRun`. We already have the concept of parent-child relationships in the `WfRunId`. The way this will work is that the child `WfRun` will have the `parent_wf_run_id` field set to the id of the parent.

There is no need to send any extra commands to the repartition or timer topologies: we can just create a `WfRunModel` for the child, and call `advance()` on it immediately.

One thing we must do: when the child terminates, we need to "wake up" the parent and call `advance()` on it. That means we should keep a flag somewhere on the protobuf of the child `WfRun` that says "hey wake up the parent please."

A quick note: if you want to set the parent `WfRunId` in `rpc RunWf`, it only works if the child `WfSpec` has a reference to the parent `WfRun`. However, this feature will work without the parent reference. You can run any `WfSpec` as a child _unless_ the `WfSpec` you are running has a parent reference to a different `WfSpec` than the caller.

### `WfSpec` Versioning

We will take advantage of the fact that we track major and minor versions. The `StartWfRunNode` will keep track of the major version of the child `WfSpec` which is to be run.

By default the SDK will set the major version as `-1`, which is a sentinel value. The `WfSpecModel#validate()` method will fill it in and set it to the latest available major version. If the value is not `-1`, then the `PutWfSpecRequestModel` will not change the value and will store it raw.

### Protobuf Structure

Protobuf is the way.

#### ThreadRun Output

We need to extend the `ExitNode` and the `ThreadRun` to support having output:

```proto
message ThreadRun {

    // ...

    // The output of the `ThreadRun`.
    optional VariableValue output = 16;
}

message ExitNode {
    // If neither failure_def nor return_content are set, then a ThreadRun arriving
    // at this ExitNode completes successfully with no output.
    oneof result {
        // If set, this ExitNode throws the specified Failure upon arrival. Note that Failures
        // are propagated up to the parent ThreadRun (or cause the entire WfRun to fail if sent
        // by the entrypoint ThreadRun).
        //
        // If this is not set, then a ThreadRun arriving at this Exit Node will be COMPLETED.
        FailureDef failure_def = 1;

        // If set, the ExitNode returns the value that comes from this VariableAssignment.
        VariableValue return_result = 2;
    }

    // If set, then the output of the ThreadRun becomes the resolution
    // of this VariableValue.
    optional VariableValue output = 2;
}
```

#### `RunChildWfNode`

The output of the `RunChildWfNode` is the output of the entrypoint ThreadRun of the child. This nod

```proto
// This node spawns a child `WfRun` and waits for the child `WfRun` to terminate
// before completing.
//
// The output of the `RunChildWfNode` is the output of the entrypoint ThreadRun
// of the child `WfRun`.
message RunChildWfNode {
  // The name of the WfSpec to spawn.
  string thread_spec_name = 1;

  // The major version of the WfSpec to spawn.
  int32 major_version = 2;

  // The input variables to pass into the Child ThreadRun.
  map<string, VariableAssignment> variables = 3;
}

// The RunChildWfNodeRun starts a Child `WfRun` and waits for its completion.
message RunChildWfNodeRun {
    // The id of the created `WfRun`.
    WfRunId child_wf_run_id = 1;

    // A record of the variables which were used to start the `WfRun`.
    map<string, VariableValue> input_variables = 2;
}
```

#### `StartChildWfNode`

```proto
// Much like the RunChildWfNode, this node spawns a child `WfRun`. However, rather
// than waiting for the child `WfRun` and returning its ouptut, this Node returns a
// handle to the resulting `WfRunId`.
message StartChildWfNode {
  // The name of the WfSpec to spawn.
  string thread_spec_name = 1;

  // The major version of the WfSpec to spawn.
  int32 major_version = 2;

  // The input variables to pass into the Child ThreadRun.
  map<string, VariableAssignment> variables = 3;
}

// The StartWfNodeRun starts a Child `WfRun` and does not wait for its completion.
// It returns a `VariableValue` containing the resulting `WfRunId`.
message StartChildWfNodeRun {
    // The id of the created `WfRun`.
    WfRunId child_wf_run_id = 1;

    // A record of the variables which were used to start the `WfRun`.
    map<string, VariableValue> input_variables = 2;
}
```

#### `WaitForChildWfNode`

```proto
// The WaitForChildWfNode accepts a VariableAssignment resolving to a WfRunId and
// waits for the specified WfRun to complete. The output is given by the output
// of the waited-for `WfRun`.
message WaitForChildWfNode {
    // Specifies the ID of the child `WfRun` to wait for.
    VariableAssignment child_wf_run_id = 1;
}

// The WaitForChildWfNodeRun waits for a `WfRun`.
message WaitForChildWfNodeRun {
    // The id of the `WfRun` this `NodeRun` is waiting for.
    WfRunId child_wf_run_id = 1;
}
```

## Disclaimers

The Force is with this proposal.

### Compatibility and Testing

We are adding new functionality to the LH Server without any deprecations. We can test this with unit tests for the SDK and e2e tests on the server side. We should also use this as an opportunity to introduce proper unit testing hygiene to the server at the `SubNodeRun` class level.

### Performance Considerations

There aren't significant performance considerations from this proposal beyond the fact that, when a child `WfRun` completes, if a parent `WfRun` reference is present we must also "wake up" and call `WfRunModel#advance()` on the parent `WfRun`. However, this is essentially the same as if the child `WfRun` were just a child `ThreadRun` within the parent `WfRun`.

## Future Work

All of the following ideas came to me while writing this prroposal, and many of them were originally part of this proposal. However, I think they are best left to future work.

### Strongly-Typed Exceptions

We are making a push towards strong typing across LittleHorse. The exception propagation across `WfSpec` boundaries means that a `EXCEPTION` is now part of the API of a `WfSpec`. We should, in the future, validate exception-catching. There is a [ticket already closed](https://github.com/littlehorse-enterprises/littlehorse/issues/500) that proposes to make a `TaskDef` advertise the exceptions that it can throw.

If we were to do that, then we would be able to flag uncaught exceptions, or at least advertise them in the `WfSpec`. This is a whole separate Proposal that requires serious thought.

### Accessing Fields on Structures

Once the `StructDef` implementation is finished, we'll have multiple well-known structures that have types in LittleHorse:

* Any `Struct`
* The output of a `UserTaskNodeRun`
* The output of a `WaitForThreadsNodeRun`
* The output of a `WaitForChildWfNodeRun`
* The output of a `WaitForChildWfsNodeRun`
* A `VariableValue` containing a `WfRun`.

Each of these structures has multiple fields, and we know the names and paths of those fields. We may want to access an individual field inside the WfSpec DSL. We should add a way to do that. Perhaps something like:

```
StructVariable myStruct = wf.declareStruct("some-struct", Car.class);

// Option 1
IntVariable year = myStruct.accessField("model").accessField("year");

// Option 2, not sure if possible
IntVariable year = myStruct.getModel().getYear();

WfRunIdVar myWfRunId = wf.declareWfRunId("child-wf-id");
myWfRunId.assign(wf.startChildWf("some-child", Map.of()));

// Option 1
myWfRunId.accessField("parent_wf_run_id");

// Option 2
myWfRunId.getParent();
```

Anyways, the above is well beyond the scope of this Proposal.

### Accessing Output And Metadata

What if I wanted to do something like this:

```
WfRunVariable customerId = wf.declareStr("customer-id");
WfRunVariable paymentMethodId = wf.declareStr("payment-method")

NodeOutput childOutput = wf.runChildWf("request-new-credit-card", Map.of("user-id", customerId));
paymentMethod.assign(childOutput);
```

### Supporting `WfRunId` Variable Type

The proposal discussed here cannot work properly without supporting `WfRunId` as an additional option inside the `VariableValue`—otherwise, we would have to return a `STR`, which poses a backwards-compatibility issue 

I believe we should do the following:

1. Implement the synchronous `RunChildWfNode`, which does not require returning a `WfRunId`.
2. Have a proper Proposal about adding `WfRunId` to the `VariableValue`.
3. Finish the implementation of this Proposal (`StartChildWfNode` and `WaitForChildWfNodeRun`) once we have the ability to use a `WfRunId` natively inside the code.

It might look like the following:

### Extending `VariableValue`

We should extend the `VariableValue` to include a `WfRunId` type. We are now passing `WfRunId`'s around inside our DSL, and we should have a better way to do that.

```proto
message VariableValue {
    oneof value {
        // ...

        // Represents a LittleHorse WfRunId.
        WfRunId wf_run_id = 11;
    }
}
```

Additionally, we could add methods to the `WorkflowThread` that allow you to do something like: `wf.getWfRunId()`.

### Improving `WfRunVariable`

In Java, at least, there are likely ways to significantly improve the way `WfRunVariable`'s work. We could potentially introduce a `IntVariable` and `StrVariable` and `StructVariable<Foo>` or something like that which would allow our users to rely on their own IDE's for more validations.

### Improving `WAIT_FOR_THREADS`

Once we implement all of the above cleanup, we should be able to improve the user-experience of `WaitForThreadsNode`s, which require using `jsonPath()`. We should have a follow-up Proposal to improve that API.
