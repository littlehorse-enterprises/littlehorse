# Waiting for Conditions

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

<!-- We really need docusaurus versioning. This feature isn't released yet...if someone finds it
they will be confused since it's not supported in our latest release.
-->

:::tip
If you want to wait for something to happen _outside the `WfRun`_, like a callback or a webhook, what you want is 
our [External Events](../04-external-events.md) feature.
:::

<!-- We also should note somewhere that this feature is only supported in the Java SDK right now. -->

Sometimes you want a single `ThreadRun` in your workflow to block until _some condition_ is true, where that condition can be represented by variables in your `WfRun`.

## Use-Cases

Use of this feature is advanced; and it only makes sense when you have Child `ThreadRun`'s or Child `WfRun`'s who can mutate the values of Variables in your parent `ThreadRun` while it waits for the condition to be true. Otherwise, the `ThreadRun` will block forever.

The three patterns for using the Wait-for-Condition feature are described below. In all three cases, a child `ThreadRun` or `WfRun` mutates the LHS variable in the condition we are waiting for.

### Child Threads

Any child `ThreadRun` may mutate the value of variables in its parent.

Most use-cases in which you want to wait for a child `ThreadRun` to do something before proceeding forward in the parent can be covered by a [`WAIT_FOR_THREADS` node](../07-child-threads.md#waiting-for-child-threads). However, sometimes a parent may want to wait for some _intermediate step_ inside the child `ThreadRun` to complete before proceeding. In this case, the `WAIT_FOR_CONDITION` provides a mechanism for the child to "wake up" the parent.

### Interrupt Handler

You can use an [Interrupt Handler](../05-interrupts.md) to mutate the value of a Variable in a `WfRun` by posting an `ExternalEvent`. This is because the Interrupt Handler causes a Child `ThreadRun` to be run, and the child `ThreadRun` may mutate variables in the parent.

For example, you might have an `ExternalEvent` that runs every 30 seconds to report the status of some infrastructure that is being deployed. The Interrupt Handler sets the `status` variable. The parent `ThreadRun` might do something like:

```java
wf.waitForCondition(wf.condition(status, EQUALS, "HEALTHY"));
```

### Child Workflows

Child workflows can also mutate `PUBLIC` variables in their parents, just like Child `ThreadRun`s. You would use Child Workflows instead of Child Threads when either:
- Forces outside of the parent `WfSpec` need to decide when to run child `WfRun`s, and Interrupts are not sufficient.
- You need to run thousands of children. A single `WfRun` should not have more than 1,000 `ThreadRun`s due to serialization performance.

## Implementation

To wait for a condition inside a `WfSpec`, you can do the following:

<Tabs>
  <TabItem value="java" label="Java" default>

```java
public void wfExample(WorkflowThread wf) {
    WfRunVariable myVar = wf.addVariable("my-var", VariableType.STR);

    // register interrupt handler or spawn child thread that may mutate `my-var`.
    // Alternatively, rely on child workflows to mutate it.

    // Once `my-var` gets set to "some-value", the thread continues
    wf.waitForCondition(wf.condition(myVar, Comparator.EQUALS, "some-value"));

    // ...
}
```

  </TabItem>
  <TabItem value="go" label="Go">

This feature is not yet supported in the Go sdk.

  </TabItem>
  <TabItem value="python" label="Python">

This feature is not yet supported in the Python sdk.

  </TabItem>
</Tabs>


:::note
In the near future, we will add the following:

- Timeouts on a `WAIT_FOR_CONDITION` Node with the ability to specify an `EXCEPTION` to throw upon timeout.
- Support for `WAIT_FOR_CONDITION` nodes in our Python and Go SDK's
:::
