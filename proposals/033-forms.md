# UI Metadata for Struct-Backed User Tasks

Author: Jake Rose

## Table of Contents

- [Motivation](#motivation)
- [Schema Options](#schema-options)
  - [Forms](#forms)
  - [Option 1: Client-Defined UI Metadata in Forms](#option-1-client-defined-ui-metadata-in-forms)
  - [Option 2: Server-Defined UI Metadata in Forms](#option-2-server-defined-ui-metadata-in-forms)
  - [Option 3: UI Metadata in StructDefs](#option-3-ui-metadata-in-structdefs)
- [Compatibility](#compatibility)
- [E2E Lifecycle](#e2e-lifecycle)
- [Future Work](#future-work)

## Motivation

Struct-backed User Tasks define typed output but need a way to describe how
clients collect that output. This proposal compares three options for where UI
metadata lives and who defines its format.

## Schema Options

The options are alternatives, not schemas to combine. Existing-message excerpts
show additions after the Struct-backed UserTask proposal. Options 1 and 2 share
the following Form references and APIs; Option 3 uses StructDef APIs instead.

### Forms

```protobuf
// user_tasks.proto
message UserTaskDef {
  // ... existing fields unchanged.
  // Optional form used to collect this task's output.
  // Allows client to reference pinned version of the form.
  FormReference form = 7;
}

// service.proto
message PutUserTaskDefRequest {
  // ... existing fields unchanged.
  // Optional form to associate with the task definition.
  FormReference form = 5;
}

// user_tasks.proto
message UserTaskRun {
  // ... existing fields unchanged.
  // Exact form version retained for this task run.
  FormId form_id = 15;
}

message FormId {
  // Name of the form.
  string name = 1;
  // Exact revision of the named form.
  int32 version = 2;
}
message FormReference {
  // Name of the form to use.
  string name = 1;
  // Exact revision to use; omitted means latest, while 0 is explicit.
  optional int32 version = 2;
}

message GetFormRequest {
  // Name of the form to fetch.
  string name = 1;
  // Exact revision to fetch; omitted means latest, while 0 is explicit.
  optional int32 version = 2;
}

message SearchFormRequest {
  // Pagination token from a previous search response.
  optional bytes bookmark = 1;
  // Maximum number of results to return.
  optional int32 limit = 2;
  // Selects the form search filter.
  oneof form_criteria {
    // Match form names starting with this prefix.
    string prefix = 3;
    // Match this exact form name.
    string name = 4;
  }
}

message DeleteFormRequest {
  // Exact form version to delete; other versions are unaffected.
  FormId id = 1;
}

message FormIdList {
  // Form identifiers returned by the search.
  repeated FormId results = 1;
  // Pagination token used to request the next page.
  optional bytes bookmark = 2;
}

rpc PutForm(PutFormRequest) returns (Form) {}
rpc GetForm(GetFormRequest) returns (Form) {}
rpc SearchForm(SearchFormRequest) returns (FormIdList) {}
rpc DeleteForm(DeleteFormRequest) returns (google.protobuf.Empty) {}
```

### Option 1: Client-Defined UI Metadata in Forms

```protobuf
message Form {
  FormId id = 1;
  StructDefId result_struct_def_id = 2;
  repeated FormField fields = 3;
  google.protobuf.Timestamp created_at = 4;
}

message FormField {
  // Root-relative path to the answer within the result Struct.
  LHPath field_path = 1;
  // Client-defined settings, including widgets, pages, and predicates.
  map<string, string> metadata = 2;
}

message PutFormRequest {
  string name = 1;
  StructDefId result_struct_def_id = 2;
  repeated FormField fields = 3;
}
```

### Option 2: Server-Defined UI Metadata in Forms

```protobuf
message Form {
  FormId id = 1;
  StructDefId result_struct_def_id = 2;
  repeated FormPage pages = 3;
  google.protobuf.Timestamp created_at = 4;
}

message FormPage {
  optional string title = 1;
  repeated FormField fields = 2;
  // Boolean expression over draft answers; unset means always visible.
  VariableAssignment visible_when = 3;
}

message FormField {
  LHPath field_path = 1;
  FormWidget widget = 2;
  optional string label = 3;
  optional string help_text = 4;
  optional string placeholder = 5;
  repeated FormOption options = 6;
  // Boolean expressions over draft answers, evaluated by the client.
  VariableAssignment visible_when = 7;
  VariableAssignment read_only_when = 8;
  VariableAssignment disabled_when = 9;
}

message FormOption {
  string label = 1;
  VariableValue value = 2;
}

enum FormWidget {
  FORM_WIDGET_AUTO = 0;
  FORM_WIDGET_TEXT = 1;
  FORM_WIDGET_TEXTAREA = 2;
  FORM_WIDGET_NUMBER = 3;
  FORM_WIDGET_CHECKBOX = 4;
  FORM_WIDGET_SELECT = 5;
  FORM_WIDGET_RADIO = 6;
  FORM_WIDGET_MULTISELECT = 7;
  FORM_WIDGET_DATE = 8;
  FORM_WIDGET_TIME = 9;
  FORM_WIDGET_DATETIME = 10;
  FORM_WIDGET_EMAIL = 11;
  FORM_WIDGET_PASSWORD = 12;
  FORM_WIDGET_PHONE = 13;
  FORM_WIDGET_TOGGLE = 14;
}

message PutFormRequest {
  string name = 1;
  StructDefId result_struct_def_id = 2;
  repeated FormPage pages = 3;
}
```

### Option 3: UI Metadata in StructDefs

```protobuf
message StructDef {
  StructDefId id = 1;
  optional string description = 2;
  google.protobuf.Timestamp created_at = 3;
  InlineStructDef struct_def = 4;
}

message InlineStructDef {
  map<string, StructFieldDef> fields = 1;
}

message StructFieldDef {
  TypeDefinition field_type = 1;
  optional VariableValue default_value = 2;
  bool is_nullable = 3;
  optional string description = 4;
  // New: client-defined UI settings stored with the field definition.
  map<string, string> metadata = 5;
}
```

## Compatibility

### Registration

#### Registering a StructDef

For Option 3, metadata is stored on StructFieldDef, not submitted Struct values.
Metadata-only changes create a new StructDef version but do not change the data
contract. `PutStructDef` must compare metadata for versioning while excluding it
from data-schema compatibility checks. The server does not interpret it.

#### Registering a Form (Options 1 and 2)

For both options, the server rejects `PutForm` if the referenced StructDef does not exist or any
field path is invalid. Paths must be non-empty, use fixed Struct field names,
and not duplicate another field's path. Nested Struct fields are supported;
array indexes and dynamic selectors are not. Invalid paths return
`INVALID_ARGUMENT`.

##### Option 1

Metadata interpretation and validation are left to clients.

##### Option 2

The server also rejects incompatible widget/type combinations, invalid option
values, and unsupported or invalid boolean predicates. The supported expression
subset must be specified if this option is selected.

#### Registering a UserTaskDef

The result StructDef must exist. For Options 1 and 2, if a FormReference is
provided, the server checks the specified Form version, or the current latest
version if omitted. Registration is rejected if that Form does not exist or its
result StructDef name differs from the UserTaskDef's result StructDef name.
The versions do not need to match: the UserTaskDef specifies a minimum version,
while the Form references an exact version. A Form requires a Struct-backed
result contract.

This check does not pin an unversioned FormReference; it still means latest at
task creation. Option 3 has no separate Form reference to check.

### Versioning

`PutForm` creates version `0` for a new name. Identical content returns the latest
existing Form; changed content creates `latest + 1`. Comparison includes the
referenced StructDef, field/page order, and presentation settings, but excludes
generated IDs and timestamps. Metadata map entry order does not affect equality.

### Runtime Behavior

#### Creating a UserTaskRun

Under the [Struct-backed UserTask proposal](028-struct-backed-user-tasks.md#schema-and-usertaskdef-versioning),
the run resolves the latest StructDef version at or above the UserTaskDef's
minimum version. For Options 1 and 2, it also resolves the specified Form version,
or the latest if omitted. The run retains those resolved IDs.

The server does not repeat Form-to-StructDef compatibility validation at task
creation. The registration check describes the definitions at registration time,
not a guarantee that independently updated Forms and StructDefs remain compatible.

#### After a StructDef or Form Update

Existing runs retain their resolved schema and Form IDs. New runs may resolve
newer versions. For Options 1 and 2, a StructDef update does not automatically
update the Form; displaying a new field requires publishing and selecting a Form
version that includes it. UI-only changes can create a Form version without a
StructDef change. For Option 3, UI metadata changes travel with the new StructDef
version, so there is no separate Form to update.

Clients must use the run's resolved output schema and handle presentation
differences. An older Form may omit a newly added nullable or defaulted field,
but removed or changed fields can make it unusable. Data-schema compatibility
does not guarantee that the UI still works. Clients render controls and evaluate
predicates, including the typed predicates in Option 2.

#### Completing a UserTaskRun

The server validates submitted answers against the run's resolved output schema
in every option. Form settings and client-side validation do not replace this
check.

## E2E Lifecycle

For Option 1:

1. Publish a StructDef defining the answer fields and their types.
2. Publish a Form referencing that StructDef, with client-defined UI metadata for its fields.
3. Publish a UserTaskDef referencing the StructDef and Form.
4. Publish and run a WfSpec using that UserTaskDef. The server creates a UserTaskRun
   and records the selected StructDef and Form versions.
5. The client fetches the task and its recorded StructDef and Form, then uses a
   reusable renderer to display the inputs.
6. The client submits the answers through `CompleteUserTaskRunRequest.output`.
   The server validates them against the task's schema and completes the task if valid.

## Future Work

SDK annotations could provide an optional convenience for generating UI metadata
from classes registered as StructDefs. They are outside the scope of this proposal
and may not be necessary if creating Forms directly through the API or client
tooling (or supplying metadata through `PutStructDef` in Option 3) is sufficient.