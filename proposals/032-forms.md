# Versioned Forms for Struct-Backed User Tasks

Author: Jake Rose


## Motivation

The Struct-backed User Task implementation defines typed output but user task still lack a configurable way to display complex data collection.

Form separates presentation from the data contract, supporting widgets and
multi-page forms and conditional visibility without adding UI metadata to StructDef.

## Proposal

Add a versioned Form that references a StructDef and contains ordered pages
of fields. UserTaskDef optionally references the form. UserTaskRun retains its
resolved version.



### Protobuf Changes

New fields below extend existing messages after the Struct-backed implementation.

#### Existing Messages

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

```

#### object_id.proto

```protobuf
message FormId {
  // Name of the form.
  string name = 1;
  // Exact revision of the named form.
  int32 version = 2;
}
```

#### form.proto



```protobuf
message FormReference {
  // Name of the form to use.
  string name = 1;
  // Exact revision to use; omitted means latest, while 0 is explicit.
  optional int32 version = 2;
}

message Form {
  // Unique name and version of this form.
  FormId id = 1;
  // Schema that the collected answers must satisfy.
  StructDefId result_struct_def_id = 2;
  // Optional heading displayed for the form.
  optional string title = 3;
  // Form pages in display order.
  repeated FormPage pages = 4;
  // Time this form version was created.
  google.protobuf.Timestamp created_at = 5;
}

message FormPage {
  // Optional heading displayed for the page.
  optional string title = 1;
  // Input fields in display order.
  repeated FormField fields = 2;
  // All conditions must pass to show the page; empty means always visible.
  repeated FormCondition visible_when = 3;
}

message FormField {
  // Root-relative path to the answer within the result Struct.
  LHPath field_path = 1;
  // Input control used to render the field.
  FormWidget widget = 2;
  // Optional display label for the input.
  optional string label = 3;
  // Optional guidance displayed alongside the input.
  optional string help_text = 4;
  // Optional hint displayed while the input is empty.
  optional string placeholder = 5;
  // Choices available for selection-based widgets.
  repeated FormOption options = 6;
  // All conditions must pass to show the field; empty means always visible.
  repeated FormCondition visible_when = 7;
}

message FormOption {
  // Display text for this choice.
  string label = 1;
  // Answer value represented by this choice.
  VariableValue value = 2;
}

message FormCondition {
  // Root-relative path to the draft answer being tested.
  LHPath field_path = 1;
  // Comparison operator applied to the answer and comparison value.
  Comparator comparator = 2;
  // Non-null value to compare the answer against.
  VariableValue value = 3;
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

```

#### service.proto

```protobuf
message PutFormRequest {
  // Name of the form to register.
  string name = 1;
  // Schema that the collected answers must satisfy.
  StructDefId result_struct_def_id = 2;
  // Optional heading displayed for the form.
  optional string title = 3;
  // Form pages in display order.
  repeated FormPage pages = 4;
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

message FormIdList {
  // Form identifiers returned by the search.
  repeated FormId results = 1;
  // Pagination token used to request the next page.
  optional bytes bookmark = 2;
}

rpc PutForm(PutFormRequest) returns (Form) {}
rpc GetForm(GetFormRequest) returns (Form) {}
rpc SearchForm(SearchFormRequest) returns (FormIdList) {}
```

### Keeping Forms in Sync with Structs

Form definitions are maintained separately from Struct definitions. When a
Struct version adds or removes fields, the corresponding form must be updated
and published as a new version to reflect those changes. Struct evolution does
not automatically add or remove form inputs or update their layout.

Under this proposal, a `WfRun` resolves unpinned form references to the latest
form version when the run starts. An explicitly pinned reference always uses
the specified version. The resolved versions remain fixed for that run, and
each `UserTaskRun` records the form version it uses. Publishing a newer form
does not change the display of an existing run.

Form and Struct updates must be coordinated so that the selected form matches
the User Task's output schema. Resolving the latest form does not change the
Struct version pinned by the workflow.





