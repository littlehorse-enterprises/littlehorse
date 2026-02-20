# Dynamic Child Workflow Spec Resolution

## Summary

This proposal extends the `RunChildWfNode` to support using a `WfRunVariable` (via `VariableAssignment`) as the child `WfSpec` name when running a child workflow.

Currently, the child `WfSpec` name must be fully specified at `WfSpec` compile time via the `RunChildWfNode.wfSpecName` field. With this proposal:

- The child `WfSpec` name can be resolved dynamically at workflow runtime from a `VariableAssignment`.
- A new, backward‑compatible field is added to `RunChildWfNode` to hold the `VariableAssignment` reference.
- The existing static `wfSpecName` behavior is preserved and continues to work unchanged.
- Validation of child workflow inputs/outputs is skipped at `WfSpec` compile time when the spec is provided dynamically and is instead performed at node arrival time.

This enables a “dynamic child workflow” pattern while preserving compatibility with previous versions.

---

## Motivation

Real‑world use cases reported by our existing users require choosing which workflow to run based on runtime variables. With this proposal, we aim to enable the following use cases as well:

- Routing to different fulfillment workflows by region or tenant.
- Running different fraud workflows depending on risk profile.
- A/B testing or phased rollouts where the child workflow spec is selected at runtime.

Today, `RunChildWfNode` requires a statically declared `wfSpecName`, so the child spec must be known at compile time. Implementing the above patterns requires workarounds (e.g., giant switch statements or wrapper workflows).

Allowing the child `WfSpec` name to be resolved from a `WfRunVariable` at runtime directly enables a clean, first‑class “dynamic child workflow” pattern:

```java
    WfRunVariable someVar = workflowThread.declareStr("some-variable");
    SpawnedChildWf childWf = workflowThread.runWf(someVar, Map.of());
    NodeOutput nodeOutput = workflowThread.waitForChildWf(childWf);
```
### API Changes

We extend `RunChildWfNode` with a new, optional field that allows specifying the child `WfSpec` name via a `VariableAssignment`:


```protobuf
// This node spawns a child `WfRun` and returns the associated WfRunId.
message RunChildWfNode {
  // Specifies how to determine the name of the WfSpec that should be executed as a child workflow.
  // This can be either a static name known at WfSpec compile time or dynamically determined at runtime.
  oneof wf_spec {
    // Static reference to a WfSpec by its exact name that will be executed as a child workflow.
    string wf_spec_name = 1;
    // Dynamic way to specify the child WfSpec name at runtime by providing a variable assignment 
    // that resolves to a STR containing the WfSpec name to execute
    VariableAssignment wf_spec_var = 4;
  }

  // The major version of the WfSpec to spawn.
  int32 major_version = 2;

  // The input variables to pass into the Child ThreadRun.
  map<string, VariableAssignment> inputs = 3;
}
```

**Rules:**

- At most one of `wfSpecName` or `wfSpecNameVar` may be set at a time (oneof).
- If `wfSpecName` is set:
    - Behavior is identical to current behavior (full compile‑time validation, static child spec).
- If `wfSpecNameVar` is set:
    - The spec name is resolved when the node is reached at workflow runtime.
    - Compile‑time validation of the child spec’s existence is **skipped** as it is not possible with the current structure.
    - Runtime validation occurs when the node arrives.

This is wire‑compatible as a non‑breaking addition under our protobuf evolution guidelines.

Additionally, this proposal also covers keeping track of the executed workflow in the `RunChildWfNodeRun` object. That's why we also add a new field to `RunChildWfNodeRun`:

```protobuf
message RunChildWfNodeRun {
  // The id of the created `WfRun`.
  WfRunId child_wf_run_id = 1;
  // If the node failed to create the child WfRun, this field will be empty.
  optional WfRunId child_wf_run_id = 1;

  // A record of the variables which were used to start the `WfRun`.
  map<string, VariableValue> inputs = 2;
  // The resolved WfSpecId of the child workflow that was started. This field is populated
  // only after the child WfRun has been successfully created and started. If the node failed
  // to create the child WfRun, this field will be empty. The ID can be used to track which
  // version of the WfSpec was actually executed.
  optional WfSpecId wf_spec_id = 3;
}
```

---

### Failure Model

For dynamically resolved child specs (`wfSpecNameVar`):

- **Resolution failures** (e.g., variable substitution errors) result in:
    - Node status: `FAILED`
    - Error type: `CHILD_FAILURE`
- **Missing child spec** (no `WfSpec` found for resolved name):
    - Node status: `FAILED`
    - Error type: `CHILD_FAILURE`

---

### Compatibility

This proposal is designed to be fully backward compatible:

- Existing `RunChildWfNode` definitions using only `wfSpecName` continue to work unchanged.
- Compile‑time validation for static child specs is preserved exactly as today.
- The new field is optional and only affects workflows that opt into the dynamic behavior.

### Future Work

- At the sdk level, we may want to set the WfSpec major version to `RunChildWfNode` to allow specifying the exact version of the child spec to run.