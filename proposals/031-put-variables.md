# Put Variables

**Author:** Eduwer Camacaro


## Motivation

Applications and operators sometimes need to modify a variable of an existing `WfRun` without changing its `WfSpec`. Examples include correcting input data, adjusting a pending sleep, and repairing state before rescuing a failed thread.

Today, a workflow can model these changes through an `ExternalEvent` and a handler that mutates the variable. This requires the workflow author to anticipate each update and express the corresponding behavior in the specification.

This proposal introduces `PutVariable`, an RPC that replaces the value of an existing `Variable` and attempts to advance the owning workflow. It returns the previous value so that the caller can observe what was replaced. External events remain appropriate when an update needs business validation or a reaction explicitly modeled in the `WfSpec`.

## Scope

The initial implementation supports whole-value replacement of one existing variable, identified by its exact `VariableId`. It does not create variables, change their declarations, or modify the `WfSpec`.

Batch updates, nested-field patches, arithmetic mutation operations, a variable revision field, and a dedicated mutation-history API are outside the initial scope.

## Public Contract

### Protobuf

Add the following request and RPC to `service.proto`:

```protobuf
message PutVariableRequest {
  // Exact ID of the existing variable to modify.
  VariableId id = 1;

  // Replacement value. An explicitly present, empty VariableValue represents null.
  VariableValue value = 2;
}

// Replaces the value of an existing Variable and attempts to advance its workflow.
// Returns the previous value, respecting the variable's masking configuration.
rpc PutVariable(PutVariableRequest) returns (VariableValue) {}
```

The existing `VariableId` identifies the owning `WfRun`, thread run number, and variable name. The RPC performs an exact lookup; it does not resolve the name through parent-thread or inherited-variable lookup. To update shared state, callers must address the variable where it is stored.

The server validates the replacement against the variable's declared `TypeDefinition` using the same compatibility rules as workflow ingress, including validation of struct fields and collection elements. It applies casts permitted by those rules before storing the value, so the stored value conforms to the declaration. An explicitly empty `VariableValue` assigns null for any declared type. An incompatible value returns `INVALID_ARGUMENT` without changing the variable or advancing the workflow.

The server rejects updates when the owning `WfRun` has status `COMPLETED`, returning `FAILED_PRECONDITION`. Other `WfRun` statuses, including `HALTED`, `ERROR`, and `EXCEPTION`, do not prevent an update. The owning `ThreadRun` status does not restrict the write.

This RPC does not enforce business rules defined in the `WfSpec`.

Both `id` and `value` must be present. The workflow ID and variable name must be nonempty, and the thread run number must be nonnegative. Omitting `value` is an error; sending an empty `VariableValue` explicitly assigns null.

The response is a snapshot of the previous value captured before replacement and before workflow advancement. For a masked variable, it contains the existing string mask rather than the previous secret. A previous null value is returned as an empty `VariableValue`.

After replacing the value, the server attempts to advance the owning `WfRun`. Active nodes may reevaluate conditions or variable-dependent behavior, including a pending sleep. Advancement does not clear halt reasons, rescue a failed thread, reopen completed nodes, or change inputs already stored in a `TaskRun`. A successful response confirms the replacement, but does not guarantee that the workflow progresses or completes; later workflow execution may also overwrite the value.

`PutVariable` does not notify or advance child `WfRun`s that inherit the updated variable. A child sleep keeps its stored deadline after a parent update. It can reevaluate that deadline if another command advances the child before its timer fires; otherwise, the existing timer remains in effect.

### lhctl

Add `lhctl put variable` using the existing CLI value-parsing conventions:

```bash
lhctl put variable <wfRunId> <threadRunNumber> <varName> [(<varType> <payload>)]

lhctl put variable <wfRunId> 0 duration INT 120
lhctl put variable <wfRunId> 0 message STR "updated"
lhctl put variable <wfRunId> 0 data JSON_OBJ '{"enabled":true}'

# Omit both type and payload to assign null.
lhctl put variable <wfRunId> 0 message
```

The command prints the previous value returned by the server.

## Rescheduling Sleep Nodes

Relative sleeps must use a stable reference time when reevaluated. Calculate their deadline as:

```text
maturationTime = NodeRun.arrivalTime + durationSeconds * 1000
```

Use this calculation both when the sleep node is entered and whenever it is checked during advancement. Remove the exclusion of `RAW_SECONDS` from `maybeRescheduleMaturation()`. Absolute timestamp and ISO date sleeps continue to resolve their deadlines from their assignments.

If the newly evaluated deadline equals the stored deadline, no timer is added. Otherwise, update the stored maturation time and schedule a new timer. This supports expressions depending on multiple variables without tracking individual variable revisions.

For a sleep entered at `12:00:00` with an initial duration of 60 seconds, an update at `12:00:20` has these effects:

| New duration | Deadline | Effect |
| --- | --- | --- |
| 120 seconds | `12:02:00` | Extends the total sleep duration |
| 10 seconds | `12:00:10` | Schedules a timer already eligible to fire |
| 60 seconds | `12:01:00` | Leaves the deadline unchanged |

Changing the duration adjusts the total sleep length measured from node arrival; it does not restart the countdown. Repeated advancement or assigning the same duration does not move the deadline forward. A node already marked as matured is not rescheduled.

Rescheduling adds a timer rather than cancelling the old one. Existing handling ignores timer commands for previous node positions and timer commands whose timestamp precedes the updated maturation time. A deadline in the past still completes through timer-command processing, rather than immediately setting the node's matured flag in `PutVariable`.
