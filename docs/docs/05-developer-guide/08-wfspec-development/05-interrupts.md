# Interrupts

As per the [Concepts Docs](../../04-concepts/04-external-events.md#interrupts), you can set up a `ThreadSpec` such that when an `ExternalEvent` of a certain type comes in, the `ThreadRun` is interrupted and an Interrupt Handler `ThreadRun` is spawned.

To do so, you can use `WorkflowThread#handleInterrupt()`. There are two required arguments:

1. The name of the `ExternalEventDef`.
2. A lambda function, interface, or `ThreadFunc` defining the handler thread (generally, this is a lambda function).

Note that when a `ThreadRun` is Interrupted, it must first halt. A `ThreadRun` is not considered `HALTED` until all of its Children are `HALTED` as well. Therefore, interrupting a `ThreadRun` causes all of the Children of the Interrupted `ThreadRun` to halt as well.

## Example

In this example, we have a `WfSpec` that defines a long-running `WfRun` that uses an email address (stored as a `WfRunVariable`) to communicate with a customer.

What if the customer changes their contact info? Let's define an `ExternalEventDef` named `email-update` whose content is a `STR` value with the new email address. We will use that `ExternalEventDef` and an Interrupt to update the `Variable` used to contact the customer.

### Variable Scoping

Recall that the interrupt handler is a Child `ThreadRun` of the Interrupted `ThreadRun`, which means that it has read and write access to the Interrupted thread's `Variable`s.

### Accessing the Event Content

`ExternalEvent`s have a payload. When you create your Handler `ThreadSpec`, you can access that content by creating a `WfRunVariable` with the name `"INPUT"`. For example, if the payload of your `ExternalEvent` will be a `JSON_OBJ`, you would do:

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

<Tabs>
  <TabItem value="java" label="Java" default>

```java
thread.addVariable(WorkflowThread.HANDLER_INPUT, VariableType.JSON_OBJ);
```

  </TabItem>
  <TabItem value="go" label="Go">

```go
thread.AddVariable("INPUT", model.VariableType_JSON_OBJ)
```
  </TabItem>
  <TabItem value="python" label="Python">

```python
thread.add_variable("INPUT",VariableType.JSON_OBJ)
```
  </TabItem>
</Tabs>


### Putting it Together

Here's a complete example:

<Tabs>
  <TabItem value="java" label="Java" default>

```java
public void threadFunction(WorkflowThread thread) {

    // The Variable used to keep track of email in the parent thread.
    WfRunVariable email = thread.addVariable("customer-email", VariableType.STR);

    // Register the Interrupt Handler
    thread.registerInterruptHandler(
        "email-update",
        handler -> {
            // Store the content of the event
            WfRunVariable eventContent = thread.addVariable(
                WorkflowThread.HANDLER_INPUT_VAR,
                VariableType.STR
            );

            // Mutate the variable
            handler.mutate(email, VariableMutationType.ASSIGN, eventContent);
        }
    )

    // Omitted: your long-running business logic that uses the `customer-email` variable
}
```

  </TabItem>
  <TabItem value="go" label="Go">

```go
func threadFunction(thread *wflib.WorkflowThread) {

    // The Variable used to keep track of email in the parent thread.
    email := thread.AddVariable("customer-email", model.VariableType_STR)

    // Register the Interrupt Handler
    thread.HandleInterrupt(
        "email-update",
        func (handler *wflib.WorkflowThread) {
            // Store the content of the event
            eventContent := handler.AddVariable(
                "INPUT", // the special name to get interrupt trigger
                model.VariableType_STR,
            )

            // Mutate the variable
            handler.Mutate(email, model.VariableMutationType_ASSIGN, eventContent)
        },
    )

    // Omitted: your long-running business logic that uses the `customer-email` variable
}
```

   </TabItem>
   <TabItem value="python" label="Python">

```python
def my_interrupt_handler(handler: WorkflowThread) -> None:
    # Get variable from parent thread
    email = handler.find_variable("customer-email")

    # Store the content of the event
    event_content = handler.add_variable("INPUT", VariableType.STR)

    # Mutate the variable
    handler.mutate(email, VariableMutationType.ASSIGN, event_content)

def my_entrypoint(wf: WorkflowThread) -> None:
    # The Variable used to keep track of email in the parent thread.
    thread.add_variable("customer-email", VariableType.STR)

    # Register the Interrupt Handler
    wf.add_interrupt_handler("email-update", my_interrupt_handler)
```

   </TabItem>
</Tabs>

## How to trigger an Interrupt event
Please refer to: [Posting External Events](../09-grpc/15-posting-external-events.md).

## Notes and Best Practices

First, only one `ThreadSpec` may register an Interrupt Handler for a given `ExternalEventDef`.

Additionally, note (as per the [Concept Docs](../../04-concepts/04-external-events.md#interrupts)) that the Interrupt Handler Thread is a Child of the Interrupted `ThreadRun`. This is a very useful feature, as it means that the Interrupt Handler [may modify](../../04-concepts/01-workflows.md#variable-scoping) the variables of the interrupted thread.

:::note
If you use an `ExternalEventDef` as a trigger for an Interrupt, you cannot reuse that `ExternalEventDef` for a [wait for `ExternalEvent`](./04-external-events.md) node.
:::