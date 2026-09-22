# Struct-Backed User Tasks

Author: Eduwer Camacaro

## Context

Current design for `UserTaskDef` aims to define the data that a human provides when completing a `UserTaskRun`. A `WfSpec` references a `UserTaskDef` from a `UserTaskNode`, and the LittleHorse Server creates a `UserTaskRun` when workflow execution reaches that node.

The current `UserTaskDef` contains a repeated list of `UserTaskField`s. The structure for these fields is:

```protobuf
// A UserTaskField is a specific field of data to be entered into a UserTaskRun.
message UserTaskField {
  // The name of the field. When a UserTaskRun is completed, the NodeOutput is a
  // single-level JSON_OBJ. Each key is the name of the field. Must be unique.
  string name = 1;
  // The type of the output. Must be a basic primitive type (STR, BOOL, INT, DOUBLE).
  VariableType type = 2;
  // Optional description which can be displayed by the User Task UI application.
  // Does not affect WfRun execution.
  optional string description = 3;
  // The name to be displayed by the User Task UI application. Does not affect
  // WfRun execution.
  string display_name = 4;
  // Whether this field is required for UserTaskRun completion.
  bool required = 5;
}
```

When a client completes a `UserTaskRun`, the Server validates the submitted field names agaist each `UserTaskField`.

Clients may also inspect `UserTaskDef.fields` to render forms for their users and submit the entered values through `CompleteUserTaskRun` or `SaveUserTaskRunProgress`. For the case of `lhctl`, it offers a interactive command that prompts for each field and submits the values in a `CompleteUserTaskRunRequest`.


### Existing Limitations

`UserTaskField` defines a separate, limited data contract for User Task results. It supports only a flat collection of primitive values and, more importantly, duplicates type information already represented by LittleHorse's `TypeDefinition` system.

This creates several limitations:

- Maintaining `UserTaskField` alongside `StructDef` creates two separate schema systems whose capabilities and validation behavior may diverge over time.
- Because the node output is untyped, the Server cannot validate downstream uses of a User Task result when registering a `WfSpec`. Type errors may instead occur while the `WfRun` is executing.
- User Task fields cannot use existing Struct features such as nullability, default values, masking, schema evolution, and reusable nested definitions.
- The output of every `USER_TASK` node is represented as a `JSON_OBJ`, regardless of the actual fields declared by the `UserTaskDef`.

Consequently, addressing these User Task limitations is essential to unlocking the full potential of Human-in-the-Loop workflows.


## Goals

- Use an exact, versioned `StructDefId` as the result contract of a new `UserTaskNode`.
- Validate completed User Task output with the existing Struct validation rules.
- Preserve the behavior of existing field-backed `UserTaskDef`s during a deprecation period.
- Preserve the ability to save incomplete User Task progress.
- Allow `lhctl` users to complete and save progress on struct-backed User Tasks.


## Public API Changes

### `UserTaskDef`

Add `result_struct_def_id` to `UserTaskDef` and deprecate `fields`.

```protobuf
message UserTaskDef {
  // The name of the UserTaskDef.
  string name = 1;

  // The version of the UserTaskDef. Only simple versioning is supported.
  int32 version = 2;

  // Metadata that does not affect WfRun execution.
  optional string description = 3;

  // Deprecated: use result_struct_def_id.
  // Present only on legacy UserTaskDefs.
  repeated UserTaskField fields = 4 [deprecated = true];

  // The time the UserTaskDef was created.
  google.protobuf.Timestamp created_at = 5;

  // The exact StructDef that defines the result of a UserTaskRun.
  // Unset for legacy UserTaskDefs that use fields.
  StructDefId result_struct_def_id = 6;
}
```

The existing `UserTaskField` message remains in the public API while legacy definitions are supported, but it is deprecated:

```protobuf
message UserTaskField {
  option deprecated = true;

  string name = 1;
  VariableType type = 2;
  optional string description = 3;
  string display_name = 4;
  bool required = 5;
}
```

### `PutUserTaskDefRequest`

Add the same StructDef reference to `PutUserTaskDefRequest` and deprecate `fields`.

```protobuf
message PutUserTaskDefRequest {
  // The name of the UserTaskDef to create.
  string name = 1;

  // Deprecated: use result_struct_def_id.
  repeated UserTaskField fields = 2 [deprecated = true];

  // Optional metadata that does not affect workflow execution.
  optional string description = 3;

  // The exact StructDef that defines the result of this UserTaskDef.
  StructDefId result_struct_def_id = 4;
}
```

During the deprecation period, the Server accepts either `fields` or `result_struct_def_id`, but not both. Supplying both returns `INVALID_ARGUMENT` because a `UserTaskDef` must have only one authoritative result schema.

The Server verifies that `result_struct_def_id` identifies an existing `StructDef` before creating the `UserTaskDef`. Registering an otherwise identical `UserTaskDef` with a different `StructDefId` creates a new `UserTaskDef` version, following the existing simple-versioning behavior.


### `CompleteUserTaskRunRequest`

Add a strongly-typed `output` and deprecate the top-level `results` map.

```protobuf
message CompleteUserTaskRunRequest {
  // The UserTaskRun to complete.
  UserTaskRunId user_task_run_id = 1;

  // Deprecated: use output.
  // Supported when completing legacy field-backed UserTaskDefs.
  map<string, VariableValue> results = 2 [deprecated = true];

  // The ID of the user who completed the task.
  string user_id = 3;

  // The output of a struct-backed UserTaskRun. Must contain a Struct value
  // compatible with UserTaskDef.result_struct_def_id.
  VariableValue output = 4;
}
```

The field is a `VariableValue` rather than a raw `Struct` so that it can use the same type-validation and node-output infrastructure as other values in LittleHorse. For a struct-backed `UserTaskDef`, the value must contain a `Struct`.

The accepted completion representation depends on the `UserTaskDef`:

| UserTaskDef type | Accepted request field | Node output |
|---|---|---|
| Legacy `fields` | `results` or compatible Struct `output` | `JSON_OBJ` |
| `result_struct_def_id` | `output` | `STRUCT` |
| No result schema | Neither | `VOID` |

Legacy field-backed definitions also accept Struct `output`. Its supplied fields must match the names and primitive types declared in `UserTaskDef.fields`, and completion requires all required fields. The server stores those values in `UserTaskRun.results`, leaves `UserTaskRun.output` unset, and continues producing a `JSON_OBJ` workflow node output. Clients do not need to supply a StructDefId for this compatibility path.

Supplying both nonempty `results` and `output`, non-Struct `output`, or legacy `results` for a Struct-backed definition will return `INVALID_ARGUMENT`.

### `SaveUserTaskRunProgressRequest`

Use the same representation as completion: Struct-backed tasks accept `output`, while legacy field-backed tasks accept either `results` or compatible Struct `output`. When saving Struct output against legacy fields, validate each supplied field but allow required fields to be omitted. Store the snapshot and saved-event values in the legacy results maps. The existing assignment policy remains unchanged.

```protobuf
message SaveUserTaskRunProgressRequest {
  UserTaskRunId user_task_run_id = 1;

  // Deprecated: supported for legacy field-backed UserTaskDefs.
  map<string, VariableValue> results = 2 [deprecated = true];

  string user_id = 3;

  enum SaveUserTaskRunAssignmentPolicy {
    FAIL_IF_CLAIMED_BY_OTHER = 0;
    IGNORE_CLAIM = 1;
  }
  SaveUserTaskRunAssignmentPolicy policy = 4;

  // A Struct value containing the current, possibly incomplete form output.
  VariableValue output = 5;
}
```

For Struct-backed tasks, `output` must contain a Struct with the exact `result_struct_def_id` of the UserTaskDef. Missing top-level fields are allowed, including required fields, and omitted top-level defaults are not applied when saving. Supplied fields must conform to their definitions; a supplied nested Struct still undergoes normal validation. Supplying both `results` and `output`, a non-Struct output, an incorrect StructDef ID, or the representation for the wrong definition type returns `INVALID_ARGUMENT`.

Saving replaces the previous progress snapshot rather than merging fields. An empty Struct with the correct ID clears the draft. Saved values remain available through `UserTaskRun.output` and the saved event's results map. Saving does not complete the task or advance the workflow; completion must still submit the full output.

### `UserTaskRun.output`

Add the same strongly typed `output` to `UserTaskRun` and deprecate `results`:

```protobuf
message UserTaskRun {
  // Deprecated: populated for legacy field-backed UserTaskRuns.
  map<string, VariableValue> results = 6 [deprecated = true];

  // Current output for a Struct-backed UserTaskRun. It may be incomplete until
  // the task is completed.
  VariableValue output = 13;
}
```

Struct-backed runs retain the actual Struct value, including its exact StructDef ID and Struct field metadata. They do not flatten fields into `results`. Legacy field-backed runs continue to populate `results` and leave `output` unset.

## SDK Experience

Before referencing a `UserTaskDef` from a workflow, the application must register the `StructDef` and `UserTaskDef` using the same exact `StructDefId`:

```java
// Set the type to the class of the schema form.
LHStructDefType type = new LHStructDefType(MyForm.class);

// Register the StructDef for the schema form.
StructDef structDef = client.putStructDef(type.toPutStructDefRequest().toBuilder()
            .setAllowedUpdates(StructDefCompatibilityType.NO_SCHEMA_UPDATES)
            .build());

// Register the UserTaskDef for the workflow.
client.putUserTaskDef(PutUserTaskDefRequest.newBuilder()
        .setName("my-user-task")
        .setResultStructDefId(structDef.getId())
        .build());

```

Then in the workflow, the application references the `UserTaskDef` (This behavior remains the same)

```java
UserTaskOutput requestOutput = wf.assignUserTask("my-user-task", userId, "testGroup");
wf.execute("my-task", requestOutput); // This passes the struct output of the UserTaskNode.
```

## Design Limitations

Struct-backed User Tasks require a separately registered, named `StructDef`. Clients first register the `StructDef`, then register a `UserTaskDef` referencing its exact `StructDefId`, and finally reference that `UserTaskDef` from the workflow.

This proposal does not support an `InlineStructDef` as the User Task's result contract or declaring a workflow-specific form directly inside the workflow builder. Even a form used by only one workflow requires separate `StructDef` and `UserTaskDef` registration. `UserTaskNode` continues to reference a registered `UserTaskDef`; it does not own a form schema.

The desired experience for inline forms is to declare them directly inside the WfSpec. Adding an `InlineStructDef` field to `PutUserTaskDefRequest` alone would not provide that experience: the form would still need separate UserTaskDef registration. Supporting workflow-local forms instead requires designing how a `UserTaskNode` owns its schema, how a `UserTaskRun` identifies that contract without a registered `UserTaskDef`, and how completion validation and form-rendering clients resolve the schema from the pinned workflow. Those changes are deferred to a separate proposal so this proposal can preserve the existing registration and lookup model while replacing the result schema with a StructDef reference.

This limitation concerns the top-level result contract. Fields within the referenced `StructDef` retain the nested types supported by the existing Struct system.


## Backward Compatibility

All new protobuf fields use previously unused field numbers. Existing field numbers are not removed or reused.

### Existing Clients

Existing clients can continue to:

- Register legacy field-backed definitions during the deprecation period.
- Render legacy definitions from `UserTaskDef.fields`.
- Complete legacy User Tasks using `CompleteUserTaskRunRequest.results`.
- Read legacy field-backed values from `UserTaskRun.results`.
- Read Struct-backed values from `UserTaskRun.output`.

Existing `lhctl execute userTaskRun` and `lhctl save userTaskRun` behavior remains unchanged for legacy field-backed definitions. A version of `lhctl` built from an older protobuf API cannot complete a struct-backed User Task and must be upgraded before those definitions are introduced.

Likewise, older clients only understand `UserTaskRun.results` and cannot read the `output` of a Struct-backed run. Clients must be upgraded before they render, save, complete, or inspect Struct-backed User Tasks.

## Author's Notes

I strongly recommend separating the data schema from the form schema. The data schema (`StructDef`) defines the submitted result and the contract that the Server validates. The form schema defines how a client presents and collects that data input, including layout, labels, widgets, conditional visibility, and multi-step interactions. Form schemas can become complex and should be able to evolve independently of the underlying data contract.

It would be useful for the LittleHorse Server to store form metadata associated with the StructDef that the form submits. A common use case is multiple form designs producing the same Struct schema—for example, a compact mobile form and a multi-step desktop form. These should share the same result contract without duplicating the StructDef or embedding presentation-specific concerns in it.

This is a recommendation for a follow-up design. This proposal establishes the Struct-backed data contract; it does not define a form-schema API or how form metadata is stored, versioned, or selected by clients.

## Future Work

Potential follow-up proposals may cover:

- Removing `UserTaskField`, `UserTaskDef.fields`, and the deprecated `results` fields from completion, progress-saving, and UserTaskRun messages in the next major API version.
- Adding Metadata Annotations for StructDef to support storing metadata about the StructDef, such as a description or a form field label.
