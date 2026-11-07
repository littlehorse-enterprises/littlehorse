# Proposal: Enum Data Type

- [Motivation](#motivation)
- [API Glossary](#api-glossary)
- [Enum Semantics](#enum-semantics)
- [Protobuf Changes](#protobuf-changes)
- [Client-Side (SDK) Changes](#client-side-sdk-changes)
- [CLI Support](#cli-support)
- [Alternatives Considered](#alternatives-considered)
- [Future Work](#future-work)

Author: Liam Mackay

This proposal describes a LittleHorse-native Enum type, similar to Java's `enum`.

## Motivation

LittleHorse already has typed records (`StructDef`, `InlineStructDef`, and `Struct`) and typed maps (`InlineMapDef` and `Map`). But, using these types to expose a fixed set of names is awkward. I propose a data-only `Enum` type that allows a selection from a predetermined set of constants.

The typing model builds on [Proposal 002](./002-move-to-strong-typing.md), and the layout follows [Proposal 023](./023-native-maps.md).

## API Glossary

| Object | Role |
|--------|------|
| `EnumDef` | a versioned enum definition. |
| `EnumDefId` | the name and version of a registered `EnumDef`. |
| `InlineEnumDef` | a `TypeDefinition` that contains a list of members. |
| `EnumMemberDef` | the definition of one member, which contains a name and a backing value. |
| `Enum` | a `VariableValue` that represents one member of an `InlineEnumDef`. |

The backing value of an Enum is significant for sorting purposes. For example, imagine a User wants to sort all workflow runs by Priority (which in this case, is an `Enum`). Sorting by the literal name is not very helpful. The `Enum` backing value allows users to define a comparison 'hierarchy' s.t. URGENT > HIGH > MEDIUM > LOW.

## Enum Semantics

### Backing Values and Declaration Order

1. If the first member has no explicit backing value, use `0`.
2. If a subsequent member has no backing value, use the backing value of the previous member plus one.
3. Backing values are `int32`.

This implies that member order matters.

Validation rules:

- Reject negative backing values.
- Reject duplicate member names or backing values.
- Reject a definition that has no members.
- Member names are case-sensitive.
- `EnumDef` names must be valid LittleHorse names (valid hostnames).

### Type Identity, Membership, and Null

- Two `EnumDef`s with different names are different types, even if their members are the same. 
- Comparisons with NULL use the existing `Comparer` behavior: NULL is equal to NULL, and NULL is less than every member regardless of backing value.
- The default value of an enum variable must be a valid member.

### Assignment and Comparisons

- `assign()` accepts a member-name literal or a native enum member.
- The server resolves member-name literals against the target's declared enum type. WfSpec validation rejects unknown members.
- `EQUALS`, `NOT_EQUALS`, `LESS_THAN`, `LESS_THAN_EQ`, `GREATER_THAN`, and `GREATER_THAN_EQ` compare enum backing values.

### Versioning and Evolution

- `NO_MEMBER_UPDATES` rejects all changes to the resolved members. It accepts description changes.
- `FULLY_COMPATIBLE_MEMBER_UPDATES` accepts only new members. It rejects member renames, renumbers, and removals.

## Protobuf Changes

### Metadata and Type Definitions

```proto
message EnumDefId {
  string name = 1;
  int32 version = 2;
}

message EnumDef {
  EnumDefId id = 1;
  optional string description = 2;
  google.protobuf.Timestamp created_at = 3;
  InlineEnumDef enum_def = 4;
}

message InlineEnumDef {
  repeated EnumMemberDef members = 1;
}

message EnumMemberDef {
  string name = 1;
  optional int32 backing_value = 2;
}
```

RPC changes:

```proto
message PutEnumDefRequest {
  string name = 1;
  optional string description = 2;
  InlineEnumDef enum_def = 3;
  EnumDefCompatibilityType allowed_updates = 4;
}

enum EnumDefCompatibilityType {
  NO_MEMBER_UPDATES = 0;
  FULLY_COMPATIBLE_MEMBER_UPDATES = 1;
}
```

### Runtime Values

```proto
message Enum {
  optional EnumDefId id = 1;
  string name = 2;
}
```

### Metadata RPCs

| API | Required behavior |
|-----|-------------------|
| `PutEnumDef` | Create or update an `EnumDef`. Each change, including a description change, creates a new version. |
| `GetEnumDef` | Get a definition by name and version. If there is no version, get the latest version. |
| `SearchEnumDef` | Search in a tenant. |
| `DeleteEnumDef` | Delete an `EnumDef` by name and version. |

## Client-Side (SDK) Changes

### Declaring and Registering Enum Types

You can declare a named enum variable with the latest version or with a specified version:

```java
wf.declareEnum("dir", "direction");     // Latest version at workflow registration.
wf.declareEnum("dir", "direction", 1);  // Exactly version 1.
```

To read the name of the Enum, call `name()`. It returns a read-only `LHExpression` that resolves to a `STR`. 

```java
WfRunVariable dir = wf.declareEnum("dir", "direction");
dir.assign("NORTH");
dir.name(); // NORTH
```

Unlike `get()` on a Struct, `name()` does not return a `WfRunVariable`, so it cannot be assigned to.

### Inline Enums

Inline enums are opt-in, the same as inline structs. A Java enum without annotations continues to map to `JSON_OBJ`. Thus, an SDK upgrade does not change the schema of existing StructDefs and TaskDefs that use Java enums.

| Position | Opt-in |
|----------|--------|
| Struct field | `@LHStructField(isInlineEnum = true)` |
| Task parameter or return | `@LHType(isInlineEnum = true)` |
| Workflow variable | `wf.declareInlineEnum(name, Direction.class)` |

The opt-in requires a Java enum class without `@LHEnumDef`. (A class with `@LHEnumDef` has already been registered as an `EnumDef`)

```java
// No @LHEnumDef: embed this definition wherever it is opted in.
public enum Direction {
    NORTH,
    EAST,
    SOUTH,
    WEST
}

@LHStructDef("journey")
public class Journey {
    @LHStructField(isInlineEnum = true)
    private Direction direction;
}
```

### Registering Named Enum Definitions

```java
@LHEnumDef("direction")
public enum Direction {
    NORTH,                    // 0
    @LHEnumValueDef(10) EAST, // 10
    SOUTH,                    // 11
    WEST                      // 12
}
```

- `@LHEnumDef("direction")` gives the name of the registered EnumDef.
- `@LHEnumValueDef(10)` gives an explicit backing value.

The Java SDK reads the annotated enum class with reflection. Then it builds and registers the ordered EnumDef:

```java
LHEnumDefType enumDefType = new LHEnumDefType(Direction.class);
client.putEnumDef(enumDefType.toPutEnumDefRequest());
```

### Branching on an Enum

Because Enums are compared via their backing value, the existing conditionals are already supported. This also means the RHS of a comparison can be a native Enum, or a string literal that resolves to the matching Enum on the server.

```java
WfRunVariable dir = wf.declareEnum("dir", "direction");
dir.assign("NORTH");
// Useful for InlineEnums
wf.doIf(dir.isEqualTo(Direction.NORTH), body -> { ... });
// Useful for EnumDefs
wf.doIf(dir.isEqualTo("NORTH"), body -> { ... });
```

- An enum variable, node output, or expression can be on the right side. For example, `dir.isEqualTo(taskOutput)` is valid if the TaskDef returns the `direction` enum.
- A right side whose type is `STR` *and* is not a literal is not accepted. For example, a `STR` variable or a TaskDef that returns `STR`. The server cannot validate this at registration time. To compare an enum with a `STR` variable, compare the member name: `dir.name().isEqualTo(strVariable)`.

## CLI Support

### `lhctl`

Add EnumDef get, search, and delete commands based on StructDef.

```bash
lhctl get enumDef order-status 0
lhctl search enumDef order
lhctl delete enumDef order-status 0
```

Enums are accepted on the CLI as case-sensitive member names:

```bash
lhctl run order-workflow status SUCCEEDED
```

## Alternatives Considered

This was brought up during a Server weekly meeting. Instead of adding a completely new container type `EnumDef`, we can try to take advantage of the existing `StructDef` implementation like so:

```proto
message StructDef {
  StructDefId id = 1;
  ...
  oneof type {
    InlineStructDef inline_struct_def = 4;
    InlineEnumDef inline_enum_def = 5;
  }
}
// OR
message InlineStructDef {
  oneof type {
    map<String, StructFieldDef> fields = 1;
    repeated EnumMemberDef members = 2;
  }
}
```

Unfortunately, Proto3 does not allow `map` and `repeated` to appear together underneath a oneof, so the second option is out.

Grouping `InlineEnumDef` underneath `StructDef` does allow re-use of existing versioning and get/search/delete logic. I have 3 problems with this approach:
  - There is now some friction inside of Client code that uses Structs. They now need to *just remember* whether a StructDef contains an Enum or a Struct. The separate `EnumDef` type avoids this entirely.
  - This is no longer a "LH native record type". A name along the lines of `CustomType` or `VersionedType` is better fitting, so if we go down this road I strongly believe the entire type should be renamed.
  - Enums and Structs cannot have the same name (since they now share an ID).

## Future Work

- **Dashboard.** Once `EnumDef` is implemented, we can render dropdown menus for Workflow executions.
- **UserTask fields.** Enum UserTask fields need `UserTaskField` to move from `VariableType` to `TypeDefinition`. Then, we can use `EnumDef`s to render custom dropdown menus.
