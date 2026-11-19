# UI Metadata for Struct-Backed User Tasks

Author: Jake Rose

## Table of Contents

- [Motivation](#motivation)
- [Design](#design)
  - [FormDef](#formdef)
  - [Placeholders](#placeholders)
  - [Runtime Form](#runtime-form)
  - [API](#api)
- [Compatibility](#compatibility)
- [E2E Lifecycle](#e2e-lifecycle)
- [Integrating with Industry Standards](#integrating-with-industry-standards)
- [Rejected Alternatives](#rejected-alternatives)
  - [Server-Defined UI Metadata](#server-defined-ui-metadata)
  - [UI Metadata in StructDefs](#ui-metadata-in-structdefs)
- [Future Work](#future-work)

## Motivation

Struct-backed User Tasks define typed output but not how clients should present
it. Clients can build their own screens, but cannot fetch task-specific UI
settings from LittleHorse and render them with a reusable component.

This proposal adds FormDefs: optional, client-defined UI metadata for the fields
of a result StructDef. WfSpecs supply workflow values through placeholders, and
each UserTaskRun returns a resolved Form ready to render.

## Design

Existing-message excerpts show additions after the
[Struct-backed UserTask proposal](029-struct-backed-user-tasks.md).

### FormDef

A FormDef holds static UI metadata for a result StructDef: form-level settings,
such as pages or a layout, and settings for individual fields. Clients
define the metadata keys and their meaning; the server stores them without
interpreting them. Each value is either a literal or a reference to a
placeholder supplied by the WfSpec.

```protobuf
message FormDef {
  FormDefId id = 1;
  StructDefId result_struct_def_id = 2;
  repeated FormFieldDef fields = 3;
  google.protobuf.Timestamp created_at = 4;
  // Client-defined settings for the whole form, such as pages or layout.
  map<string, FormMetadataValue> metadata = 5;
}

message FormFieldDef {
  // Root-relative path to the answer within the result Struct.
  LHPath field_path = 1;
  // Client-defined settings such as labels, widgets, and pages.
  map<string, FormMetadataValue> metadata = 2;
}

message FormMetadataValue {
  oneof value {
    VariableValue literal = 1;
    // Name of a placeholder supplied by the user task node.
    string placeholder = 2;
  }
}

// user_tasks.proto
message UserTaskDef {
  // ... existing fields unchanged.
  optional FormDefReference form_def = 7;
}
```

### Placeholders

The WfSpec's user task node supplies placeholder values as VariableAssignments.
The FormDef decides which metadata key each placeholder fills, at the form level
or on a field, so the WfSpec only needs placeholder names. One placeholder may
fill several keys.

Placeholders are metadata values, not keys. If the key were the placeholder,
`withPlaceholder("label", ...)` would set the value of the `label` key like a
normal key-value update. Since every field can have a `label`, it would set them
all to the same value. Keys should only name UI settings, such as `label` or
`widget`. Referencing the placeholder as a value lets the FormDef choose exactly
which field and key each placeholder fills.

```protobuf
// wf_spec.proto
message UserTaskNode {
  // ... existing fields unchanged.
  map<string, VariableAssignment> placeholders = 8;
}
```

```java
wf.assignUserTask("review-order", null, "reviewers")
    .withPlaceholder("greeting", wf.format("Hello {0}, approve this order?", customerName))
    .withPlaceholder("regions", allowedRegions);
```

Text is built with the existing `format` support, so the server never parses
metadata strings. Placeholders can hold any type, such as a list of options.

### Runtime Form

When it creates a UserTaskRun, the server copies the FormDef's metadata and
fields into a Form. Literals are kept, and each placeholder reference is replaced
by its resolved value. Clients get everything needed to render from the UserTaskRun.

```protobuf
// user_tasks.proto
message UserTaskRun {
  // ... existing fields unchanged.
  Form form = 15;
}

message Form {
  FormDefId form_def_id = 1;
  repeated FormField fields = 2;
  map<string, VariableValue> metadata = 3;
}

message FormField {
  LHPath field_path = 1;
  map<string, VariableValue> metadata = 2;
}
```

For example, this FormDef field:

```json
{
  "fieldPath": "approved",
  "metadata": {
    "label": { "placeholder": "greeting" },
    "widget": { "literal": { "str": "checkbox" } }
  }
}
```

becomes this Form field on the UserTaskRun:

```json
{
  "fieldPath": "approved",
  "metadata": {
    "label": { "str": "Hello Jake, approve this order?" },
    "widget": { "str": "checkbox" }
  }
}
```

### API

```protobuf
message FormDefId {
  string name = 1;
  int32 version = 2;
}

message FormDefReference {
  string name = 1;
  // Set pins this version; omitted means latest when each UserTaskRun is created.
  optional int32 version = 2;
}

// service.proto
message PutUserTaskDefRequest {
  // ... existing fields unchanged.
  optional FormDefReference form_def = 5;
}

message PutFormDefRequest {
  string name = 1;
  StructDefId result_struct_def_id = 2;
  repeated FormFieldDef fields = 3;
  map<string, FormMetadataValue> metadata = 4;
}

message GetFormDefRequest {
  string name = 1;
  // Omitted means latest; 0 is explicit.
  optional int32 version = 2;
}

message SearchFormDefRequest {
  optional bytes bookmark = 1;
  optional int32 limit = 2;
  oneof form_def_criteria {
    string prefix = 3;
    string name = 4;
  }
}

message DeleteFormDefRequest {
  // Exact version; other versions are unaffected.
  FormDefId id = 1;
}

message FormDefIdList {
  repeated FormDefId results = 1;
  optional bytes bookmark = 2;
}

rpc PutFormDef(PutFormDefRequest) returns (FormDef) {}
rpc GetFormDef(GetFormDefRequest) returns (FormDef) {}
rpc SearchFormDef(SearchFormDefRequest) returns (FormDefIdList) {}
rpc DeleteFormDef(DeleteFormDefRequest) returns (google.protobuf.Empty) {}
```

## Compatibility

### Registration

#### Registering a FormDef

`PutFormDef` is rejected if the referenced StructDef does not exist or any field
path is invalid. Paths must be non-empty, use fixed Struct field names, and not
duplicate another field's path. Nested Struct fields are supported; array
indexes and dynamic selectors are not. Placeholder names must be non-empty.
Invalid requests return `INVALID_ARGUMENT`.

The server does not validate metadata keys or literal values; their meaning is
left to clients.

#### Registering a UserTaskDef

If a FormDef is specified, `PutUserTaskDef` checks the given version, or the
current latest if omitted. The reference is stored as given, so an omitted
version is not pinned. Registration is rejected if:

- The FormDef does not exist.
- The UserTaskDef does not have a Struct-backed result.
- The FormDef's result StructDef name differs from the UserTaskDef's. Versions
  do not need to match: the UserTaskDef specifies a minimum version, while the
  FormDef references an exact version or name.

#### Registering a WfSpec

`PutWfSpec` checks each user task node against its UserTaskDef's pinned FormDef
version, or the current latest if unpinned. Registration is rejected if:

- The FormDef references a placeholder the node does not supply.
- The node supplies a placeholder the FormDef does not reference.

For an unpinned FormDef, a later version may reference placeholders the node does
not supply. See [Creating a UserTaskRun](#creating-a-usertaskrun).

### Versioning

`PutFormDef` creates version `0` for a new name. Identical content returns the
latest existing FormDef; changed content creates `latest + 1`. Comparison
includes the referenced StructDef, field order, form-level and field metadata
keys, and whether each value is a literal or placeholder. It excludes generated IDs and timestamps.
Metadata map entry order does not affect equality.

FormDefs resolve like StructDefs. If the UserTaskDef sets a FormDef version, that
version is pinned. If not, each new UserTaskRun uses the latest FormDef version
at creation, so publishing a FormDef version updates new runs without a new
UserTaskDef or WfSpec. `PutUserTaskDef` includes the FormDef reference in its
change detection.

### Runtime Behavior

#### Creating a UserTaskRun

Under the [Struct-backed UserTask proposal](029-struct-backed-user-tasks.md#schema-and-usertaskdef-versioning),
the run resolves the latest StructDef version at or above the UserTaskDef's
minimum version. The FormDef resolves to the pinned version, or the latest if
unpinned. `form.form_def_id` records the version used.

The server evaluates the node's placeholder assignments in the executing thread
and builds `UserTaskRun.form`: form-level and field metadata literals are copied
and placeholder references
are replaced with their resolved values. Field paths and order are preserved.
The Form is a snapshot; later workflow variable changes do not update it.

If a placeholder fails to evaluate, the node fails with `VAR_SUB_ERROR`, the same
as existing user task node assignments such as `notes`, `user_id`, and
`user_group`. If the FormDef references a placeholder the node does not supply,
which can happen after an unpinned FormDef is updated, that metadata entry is
omitted and the task is still created.

The server does not repeat FormDef-to-StructDef compatibility validation at task
creation.

#### After a StructDef or FormDef Update

Existing runs retain their resolved schema and Form. New runs may resolve newer
StructDef versions. A StructDef update does not change the FormDef; displaying a
new field requires a new FormDef version. New runs use it automatically if the
FormDef is unpinned. If it is pinned, a new UserTaskDef version and a WfSpec
registered with it are also required.

Clients must use the run's resolved output schema and handle presentation
differences. An older FormDef may omit a newly added nullable or defaulted field,
but removed or changed fields can make it unusable. Data-schema compatibility
does not guarantee that the UI still works.

#### Completing a UserTaskRun

The server validates submitted answers against the run's resolved output schema.
Form settings and client-side validation do not replace this check.

## E2E Lifecycle

1. Publish a StructDef defining the answer fields and their types.
2. Publish a FormDef referencing that StructDef, with literal metadata and
   placeholder references for its fields.
3. Publish a UserTaskDef referencing the StructDef and FormDef. Set a FormDef
   version to pin it, or omit it to use the latest for each new run.
4. Publish a WfSpec whose user task node supplies the FormDef's placeholders. The
   server checks that they match.
5. Run the WfSpec. The server evaluates the placeholders and stores the resolved
   Form on the UserTaskRun.
6. The client fetches the UserTaskRun and its resolved StructDef, then renders
   `form` with a reusable renderer.
7. The client submits the answers through `CompleteUserTaskRunRequest.output`.
   The server validates them against the task's schema and completes the task if valid.

## Integrating with Industry Standards

LittleHorse does not depend on any form library. Because metadata keys are
client-defined, a client can adopt an existing library's conventions. For
example, the MIT-licensed [SurveyJS Form Library](https://github.com/surveyjs/survey-library)
renders an interactive form from JSON.

If FormDef metadata uses SurveyJS property names, a UserTaskRun returns this Form:

```json
{
  "metadata": {
    "title": { "str": "Order review" },
    "showProgressBar": { "str": "top" }
  },
  "fields": [
    {
      "fieldPath": "approved",
      "metadata": {
        "page": { "str": "review" },
        "type": { "str": "boolean" },
        "title": { "str": "Hello Jake, approve this order?" }
      }
    },
    {
      "fieldPath": "reason",
      "metadata": {
        "page": { "str": "review" },
        "type": { "str": "comment" },
        "title": { "str": "Why not?" },
        "visibleIf": { "str": "{approved} = false" }
      }
    },
    {
      "fieldPath": "region",
      "metadata": {
        "page": { "str": "shipping" },
        "type": { "str": "dropdown" },
        "title": { "str": "Region" },
        "choices": { "jsonArr": "[\"US\", \"EU\"]" }
      }
    }
  ]
}
```

A small adapter groups fields by `page` and copies the remaining metadata into
SurveyJS elements:

```ts
function toSurveyJson(form: Form) {
  const pages = new Map<string, object[]>();
  for (const field of form.fields) {
    const { page = "main", ...props } = toJson(field.metadata);
    if (!pages.has(page)) pages.set(page, []);
    pages.get(page)!.push({ name: toName(field.fieldPath), ...props });
  }
  return {
    ...toJson(form.metadata),
    pages: [...pages].map(([name, elements]) => ({ name, elements })),
  };
}
```

It produces this SurveyJS JSON:

```json
{
  "title": "Order review",
  "showProgressBar": "top",
  "pages": [
    {
      "name": "review",
      "elements": [
        { "name": "approved", "type": "boolean", "title": "Hello Jake, approve this order?" },
        { "name": "reason", "type": "comment", "title": "Why not?", "visibleIf": "{approved} = false" }
      ]
    },
    {
      "name": "shipping",
      "elements": [
        { "name": "region", "type": "dropdown", "title": "Region", "choices": ["US", "EU"] }
      ]
    }
  ]
}
```

The client renders it and submits the answers as the task output:

```ts
const survey = new Model(toSurveyJson(run.form));
survey.onComplete.add(({ data }) =>
  client.completeUserTaskRun({ userTaskRunId: run.id, output: toStruct(data), userId }),
);
```

`toJson` unwraps `VariableValue`s, `toName` converts an `LHPath` to a field name,
and `toStruct` converts `{ "approved": false, "reason": "Too expensive",
"region": "EU" }` into a Struct. SurveyJS evaluates `visibleIf`; the server
still validates the submitted Struct against the run's output schema.

## Rejected Alternatives

### Server-Defined UI Metadata

Forms with typed pages, widgets, options, and visibility predicates defined in
protobuf:

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

Rejected because the server would own the UI vocabulary: every new widget or
setting requires protobuf, server, and SDK changes. Clients still implement
rendering and predicate evaluation, and the supported expression subset would
need its own specification. Client-defined metadata covers the same needs while
keeping UI conventions out of the server.

### UI Metadata in StructDefs

Client-defined metadata stored directly on StructDef fields:

```protobuf
message StructFieldDef {
  TypeDefinition field_type = 1;
  optional VariableValue default_value = 2;
  bool is_nullable = 3;
  optional string description = 4;
  // New: client-defined UI settings stored with the field definition.
  map<string, string> metadata = 5;
}
```

Rejected because it couples presentation to the data schema. A UI-only change
creates a new StructDef version, each StructDef version has exactly one
presentation, and `PutStructDef` would need to exclude metadata from
compatibility checks. It also has no natural place for workflow placeholders.

## Future Work

SDK annotations could provide an optional convenience for generating UI metadata
from classes registered as StructDefs. They are outside the scope of this proposal
and may not be necessary if creating FormDefs directly through the API or client
tooling is sufficient.