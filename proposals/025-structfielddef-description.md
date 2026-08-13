# 025 - StructFieldDef Description

**Author:** LittleHorse Contributors  
**Status:** Draft

## Motivation

`StructDef` already supports a top-level `description` field so developers can document what a struct represents. However, individual fields within a `StructDef` have no equivalent — `StructFieldDef` only carries type constraints (`field_type`, `default_value`, `is_nullable`). With LLM-based tooling increasingly consuming `StructDef` schemas to understand data shapes, field-level descriptions are a practical necessity.

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

### lhctl

`lhctl get structDef <name> <version>` should display field descriptions alongside field types.

## Immutability

Issued `StructDef` versions are immutable. Adding or changing a `description` on a field requires putting a new `StructDef`, which bumps the version (same as any other change). The existing `StructDefCompatibilityType` rules apply unchanged — a description-only change is treated the same as any other update.

This keeps the versioning model simple and predictable: consumers can always rely on a given `(name, version)` pair being a fixed, permanent snapshot.

## Backwards Compatibility

- The field is `optional` with a proto3 zero value of empty string, so all existing serialized `StructFieldDef`s deserialize cleanly — they simply have no description.
- No changes to `StructDefCompatibilityType` or the `FULLY_COMPATIBLE_SCHEMA_UPDATES` / `NO_SCHEMA_UPDATES` semantics.
- No changes to `InlineStructDefUtil.getIncompatibleFields()` are required. A description change is treated as a structural change and handled by the existing version-bump path.

## Future Evolution

If additional field-level metadata is wanted later (e.g. `display_name`, `example`, `deprecated`, `tags`), the natural migration is to introduce a `StructFieldMeta` message and replace the bare `description` string with it:

```protobuf
// Future — not part of this proposal
message StructFieldMeta {
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
  // description (field 4) migrated into meta.description
  optional StructFieldMeta meta = 5;
}
```

Because proto3 field numbers are permanent, field 4 (`description`) would be reserved and the content migrated to `meta.description` in any SDK that reads both. This is a well-understood proto evolution pattern and does not break existing serialized data.
