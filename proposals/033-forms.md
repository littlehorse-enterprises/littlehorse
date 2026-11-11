# Versioned Forms for Struct-Backed User Tasks

Author: Jake Rose

## Table of Contents

- [Motivation](#motivation)
- [Proposal](#proposal)
  - [Allowed Field Paths](#allowed-field-paths)
- [Protobuf Changes](#protobuf-changes)
  - [Existing Messages](#existing-messages)
  - [object_id.proto](#object_idproto)
  - [form.proto](#formproto)
  - [service.proto](#serviceproto)
- [Form Publication](#form-publication)
- [Form Version Resolution](#form-version-resolution)
- [Alternative: UI Metadata on StructDef](#alternative-ui-metadata-on-structdef)
- [Future Work](#future-work)

## Motivation

The Struct-backed User Task implementation defines typed output but user task still lack a configurable way to display complex data collection.

Form separates presentation from the data contract, supporting widgets and
multi-page forms and conditional visibility without requiring UI metadata in
StructDef.

## Proposal

Add a versioned Form that references a StructDef and contains an ordered list
of FormFields. Each field identifies an answer through an LHPath and carries
client-defined `map<string, string>` metadata. UserTaskDef optionally references
the form. UserTaskRun retains its resolved version.

When a Form is published, the server validates each `FormField.field_path`
against the exact StructDef referenced by `result_struct_def_id`. Publication
is rejected if the referenced schema cannot be found or a field path cannot be
resolved within it, with an error identifying the invalid reference or path.
This catches invalid field mappings without interpreting presentation metadata.

The server does not validate metadata or check compatibility with the task's
resolved output schema at task creation. Clients are responsible for ensuring
that a Form matches that schema, including any differences from the Form's
referenced StructDef.
Clients own presentation conventions, including widgets, labels, page grouping,
options, and predicates. These conventions can evolve without protobuf changes;
clients must agree on a metadata format to render the same Form consistently.

### Allowed Field Paths

`field_path` identifies an answer starting at the root of the result Struct.
When publishing a Form:

- Paths must be present, non-empty, and unique by their sequence of keys.
- Every selector must be a static `key` naming a Struct field. Nested paths
  can only pass through Struct-typed fields.
- Array indexes and dynamic selectors are not supported. A path can target
  a whole array, map, or JSON field, but cannot select values inside it.

The server rejects invalid paths with `INVALID_ARGUMENT`.

For example, this path selects `city` inside the nested `address` Struct:

```json
{
  "fieldPath": {
    "path": [
      { "key": "address" },
      { "key": "city" }
    ]
  },
  "metadata": {}
}
```


## Protobuf Changes

New fields below extend existing messages after the Struct-backed implementation.

### Existing Messages

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

### object_id.proto

```protobuf
message FormId {
  // Name of the form.
  string name = 1;
  // Exact revision of the named form.
  int32 version = 2;
}
```

### form.proto



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
  // Result schema used to validate field paths at Form publication.
  StructDefId result_struct_def_id = 2;
  // Form fields in default display order; clients may group them using metadata.
  repeated FormField fields = 3;
  // Time this form version was created.
  google.protobuf.Timestamp created_at = 4;
}

message FormField {
  // Root-relative path to the answer within the result Struct.
  LHPath field_path = 1;
  // Client-defined presentation settings, stored without server interpretation.
  map<string, string> metadata = 2;
}

```

### service.proto

```protobuf
message PutFormRequest {
  // Name of the form to register.
  string name = 1;
  // Result schema used to validate field paths at Form publication.
  StructDefId result_struct_def_id = 2;
  // Form fields in default display order; clients may group them using metadata.
  repeated FormField fields = 3;
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

## Form Publication and versioning

`PutForm` creates version `0` for a new name. For an existing name, it compares
the request with the latest version: identical content returns that Form unchanged;
changed content creates the next version (`latest + 1`). Earlier versions remain
unchanged.

Content includes `result_struct_def_id`, field order, field paths, and metadata.
Metadata maps are compared by their keys and string values, regardless of entry
order. Server-generated IDs and creation timestamps are excluded from comparison.

When a `UserTaskRun` with a FormReference is created, the server resolves the
specified Form version if present, or the latest version at that time if omitted.
The UserTaskRun records the resolved FormId and retains it for its lifetime;
publishing a newer Form does not change the Form version of an existing run.

Publication-time path validation does not guarantee compatibility with the task's
resolved StructDef. Form version resolution does not revalidate field paths or
check metadata or schema compatibility, and incompatibility does not block task
creation. Clients handle presentation and schema differences. The server still
validates submitted task output against the task's resolved output schema.

## Alternative: UI Metadata on StructDef

Instead of creating a separate Form object, UI metadata could be attached
directly to the StructDef. Developers could supply this metadata as part of
registering the StructDef, rather than publishing a separate Form version.
The metadata could describe labels, input
controls, pages, field order, and rules for when to show a field or page.

This keeps the data schema and its presentation together, but means a UI-only
change creates a new StructDef version. It also gives a StructDef one associated
form layout, while separate Form objects can provide different layouts for the
same StructDef.

| Topic | UI metadata stored in StructDef | Separate Form |
| --- | --- | --- |
| Creating forms | Keeps field types and UI settings together, making a default form easy to generate. | Defines and publishes the form separately, referencing the result StructDef. |
| Complex layouts | Could describe pages, field order, and rules that show fields based on other answers through client-defined metadata. | Stores page settings, field order, and rules in metadata without changing how the Struct's data is organized. |
| Representing different forms with the same StructDef | Gives the StructDef one default form layout. UserTasks that need different layouts would need extra settings to define and choose each layout. Without those settings, each layout would need a separate StructDef with the same fields and types but different UI metadata. | Each UserTask can use a different named Form with the same StructDef. |
| Changing how a form looks | Changing UI metadata requires a new StructDef version, even if the fields and types stay the same. This can be confusing because the data contract gets a new version only because the UI changed. | Creates a new Form version while keeping the same StructDef version. |
| Adding or removing Struct fields | UI settings stay with their fields, so there is less to update separately. Page layouts and rules that use those fields still need to be checked. | The existing Form can stay unchanged if it still works with the new StructDef version. A new Form version is needed to change what the UI shows. |
| Reading forms from clients | Clients fetch the StructDef to read its UI metadata. | Clients fetch the Form to read its UI metadata. |
| Runtime behavior | New UserTaskRuns resolve the latest StructDef version when they are created, so they automatically get its updated UI metadata and form layout. No separate Form needs to be published. | At task creation, the specified Form version is resolved, or the latest if omitted. Existing runs keep their resolved Form version. Compatibility with the task's resolved output schema is the client's responsibility, not a runtime server check. |

## Future Work

SDK annotations could provide an optional convenience for generating Forms from
classes registered as StructDefs. They are outside the scope of this proposal
and may not be necessary if creating Forms directly through the API or client
tooling is sufficient.