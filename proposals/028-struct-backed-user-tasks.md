# Struct-Backed User Tasks

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


## Existing Limitations

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
| Legacy `fields` | `results` | `JSON_OBJ` |
| `result_struct_def_id` | `output` | `STRUCT` |
| No result schema | Neither | `VOID` |

Supplying both `results` and `output`, or supplying the representation that does not match the `UserTaskDef`, returns `INVALID_ARGUMENT`.

### `UserTaskRun.results`

`UserTaskRun.results` remains unchanged during the initial migration:

```protobuf
map<string, VariableValue> results = 6;
```

## Backward Compatibility

All new protobuf fields use previously unused field numbers. Existing field numbers are not removed or reused.

### Existing Clients

Existing clients can continue to:

- Register legacy field-backed definitions during the deprecation period.
- Render legacy definitions from `UserTaskDef.fields`.
- Complete legacy User Tasks using `CompleteUserTaskRunRequest.results`.
- Read all completed values from `UserTaskRun.results`.

Existing `lhctl execute userTaskRun` and `lhctl save userTaskRun` behavior remains unchanged for legacy field-backed definitions. A version of `lhctl` built from an older protobuf API cannot complete a struct-backed User Task and must be upgraded before those definitions are introduced.

### Output Compatibility

Legacy User Task nodes continue to produce `JSON_OBJ`. Only definitions explicitly registered with `result_struct_def_id` produce Struct output, so existing WfSpecs retain their current output behavior.

Changing an existing UserTaskDef name from a legacy field schema to a StructDef-backed schema creates a new UserTaskDef version. Existing WfSpecs remain pinned to the previous version until they are re-registered.

## Future Work

Potential follow-up proposals may cover:

- Removing `UserTaskField`, `UserTaskDef.fields`, and completion `results` in the next major API version.


