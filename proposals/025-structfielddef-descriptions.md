# StructFieldDef Descriptions

## Motivation

`StructDef` already supports a top-level `description` field so developers can document what a struct represents. However, individual fields within a `StructDef` have no equivalent — `StructFieldDef` only carries type constraints (`field_type`, `default_value`, `is_nullable`). When reading existing `StructDef` schemas, just a name for a field is not enough to determine its use case. For both human users and LLMs trying to interpret a `StructDef` schema, having field-level descriptions would be helpful.

## Proposal

Add an `optional string description` field to `StructFieldDef`.

### Protobuf Change

In `schemas/littlehorse/type_definition.proto`:

```protobuf
message StructFieldDef {
  TypeDefinition field_type = 1;
  optional VariableValue default_value = 2;
  bool is_nullable = 3;
  optional string description = 4;  // NEW
}
```

### SDK Change

In the Java SDK, `@LHStructField` gains a `description` attribute:

```java
@LHStructField(description = "The user's primary contact email")
private String emailAddress;
```

Other SDKs (Python, Go, .NET) follow the same pattern using their existing annotation/decorator/attribute mechanisms.

## Immutability

Issued `StructDef` versions are immutable. Adding or changing a `description` on a field requires putting a new `StructDef`, which bumps the version (same as any other change).

## Backwards Compatibility

Old StructDefs created before StructFieldDef descriptions will be treated as StructDefs with no description. A StructDef issued before this proposal and a StructDef issued after this proposal (where descriptions are apart of the StructFieldDef proto) should be equal in the eyes of the server and should not issue a version upgrade.

## Future Evolution

Similar to `UserTaskDef`s and `UserTaskField`s, if additional field-level metadata is wanted later (e.g. `display_name`, `example`, `deprecated`, `tags`), the natural migration is to introduce a `StructFieldDefMetadata` message and replace the bare `description` string with it:

```protobuf
// Metadata that can be associated with a StructFieldDef to explain how its used.
message StructFieldDefMetadata {
  optional string description = 1;
  optional string display_name = 2;
  optional VariableValue example = 3;
  bool deprecated = 4;
  repeated string tags = 5;
}

message StructFieldDef {
  TypeDefinition field_type = 1;

  optional VariableValue default_value = 2;

  bool is_nullable = 3;
  
  reserved 4;

  // Optional metadata associated with this StructFieldDef.
  optional StructFieldMetadata meta = 5;
}
```

Because proto3 field numbers are permanent, field 4 (`description`) would be reserved and the content migrated to `meta.description` in any SDK that reads both. This is a common proto evolution pattern and does not break existing serialized data.
