---
sidebar_label: Interrupts
---

# Interrupts

In C and C++, you can register a handler function to handle an OS signal: when the signal is caught, the program gets interrupted and the interrupt handler is executed.

In LittleHorse, you can handle an `ExternalEvent` and trigger an Interrupt `ThreadRun`.

## Behavior

When an `ExternalEvent` is posted to a `WfRun`, and a `ThreadRun` has registered an Interrupt for that specific `ExternalEventDef`, then the affected `ThreadRun` is halted. Once the `ThreadRun` is halted, then a new Child `ThreadRun` (specifically, an Interrupt Handler) is created.

Once the Interrupt Handler (Child) completes, the Interrupted Thread (Parent) is resumed. If the Interrupt Handler fails, then the Parent also fails with a `CHILD_FAILED` exception. This error is unrecoverable.

### Variable Scoping

The Interrupt Handler `ThreadRun` is a Child of the Interrupted ThreadRun. As described in the [Child ThreadRun Docs](./08-child-threads.md#variable-scoping), this means that the Interrupt Handler has access to all variables in the scope of the interrupted ThreadRun.

## `ExternalEvent` Payload

Recall that an `ExternalEvent` has a payload, which is a `VariableValue`. The Interrupt Handler thread can access that value through the `"INPUT"` `Variable`. Recall from the [Exception Handler Docs](./10-exception-handling.md) that `"INPUT"` is a reserved `Variable` name used for the same purpose.

## Scoping

Interrupts are registered at the `ThreadSpec` level. Only one `ThreadSpec` may register an Interrupt for a specific `ExternalEventDef`.

When a `ThreadRun` is Interrupted, it must first halt. As per the [WfRun Documentation](./01-workflows.md#lifecycle), a `ThreadRun` is not considered `HALTED` until all of its Children are `HALTED` as well. Therefore, interrupting a `ThreadRun` causes all of the Children of the Interrupted `ThreadRun` to halt as well.

The Interrupt Handler is a Child of the Interrupted Thread. Therefore, it has read/write access to all of the Interrupted Thread's `Variable`s.

## Use Cases

Interrupts may be used for various reasons, such as to:

- Kill a running `WfRun` and perform some cleanup action (such as notifying a customer) before making the Interrupted Thread fail.
- Update the value of some `Variable` in a running `WfRun`, such as to change contact info or add items to a shopping cart.
- Send heartbeats from an external system and allow the `WfRun` to keep track of the last seen activity (such as when determining when to invalidate an access token due to inactivity).
