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

### Assignment and Comparisons

- Comparisons with NULL use the existing `Comparer` behavior: NULL is equal to NULL, and NULL is less than every member regardless of backing value.
- The default value of an enum variable must be a valid member.
- `assign()` accepts a member-name literal or a native enum member.
- The server resolves member-name literals against the target's declared enum type. WfSpec validation rejects unknown members.
- `EQUALS`, `NOT_EQUALS`, `LESS_THAN`, `LESS_THAN_EQ`, `GREATER_THAN`, and `GREATER_THAN_EQ` compare enum backing values.
- At workflow registration time, `InlineEnumDef`s are compared structurally. That is, even if one definition is a subset of another, it will be rejected unless they are exact matches. At runtime, values are still checked for legality.

### StructDef versioning

Adding, removing, or renumbering members in an `InlineEnumDef` would be considered a full type change. When a `StructDef` field is required, its type cannot change. So, a `StructDef` that has a required `InlineEnumDef` as a field type is locked into that `InlineEnumDef`. It cannot add, remove, or renumber members. Optional fields are not included in this check.

### Server-side error handling

If a TaskRun returns an illegal Enum value, it will fail with a TASK_OUTPUT_SERDE_ERROR. The NodeRun will fail with VAR_MUTATION_ERROR, which can be caught and handled. For dynamic tasks, since we can't check the return value at runtime we can fail with a VARR_SUB_ERROR if the return value is not a valid enum.

One case that remains open is what to do if a TaskRun sends a valid Enum member, but as a string (i.e., STR NORTH intead of ENUM NORTH). We could convert this implicitly to an Enum, or reject it as an incorrect type. I personally prefer the stricter approach of rejecting this, but can see the merit of converting it.

## Protobuf Changes

### Type Definitions

```proto
message InlineEnumDef {
  repeated EnumMemberDef members = 1;
}

message EnumMemberDef {
  string name = 1;
  optional int32 backing_value = 2;
}
```

### Runtime Values

```proto
message Enum {
  string name = 1;
}
```

## Client-Side (SDK) Changes

### Declaring Enum Types

```java
public enum Direction {
    NORTH,                    // 0
    @LHEnumValueDef(10) EAST, // 10
    SOUTH,                    // 11
    WEST                      // 12
}
WfRunVariable dir = wf.declareInlineEnum("dir", Direction.class);
```

> `@LHEnumValueDef(10)` gives an explicit backing value.

To read the name of the Enum, call `name()`. It returns a `LHExpression` that resolves to a `STR`. 

```java
dir.assign(Direction.NORTH);
// Or:
dir.assign("NORTH");
dir.name(); // NORTH
```

Unlike `get()` on a Struct, `name()` does not return a `WfRunVariable`, so it cannot be assigned to.

### Inline Enums

Inline enums are opt-in, the same as inline structs. A Java enum without annotations continues to map to `JSON_OBJ`. Thus, an SDK upgrade does not change the schema of existing `WfSpec`s and `TaskDef`s that use Java enums.

| Position | Opt-in |
|----------|--------|
| Struct field | `@LHStructField(isInlineEnum = true)` |
| Task parameter or return | `@LHType(isInlineEnum = true)` |
| Workflow variable | `wf.declareInlineEnum(name, Direction.class)` |

> Note that since StructDefs don't allow JSON_OBJ fields, this may be unnecessary.
```java
@LHStructDef("journey")
public class Journey {
    @LHStructField(isInlineEnum = true)
    private Direction direction;
}
```

### Branching on an Enum

Because Enums are compared via their backing value, the existing conditionals are already supported. This also means the RHS of a comparison can be a native Enum, or a string literal that resolves to the matching Enum on the server.

```java
WfRunVariable dir = wf.declareInlineEnum("dir", Direction.class);
dir.assign(Direction.NORTH);
// Or:
dir.assign("NORTH");
wf.doIf(dir.isEqualTo(Direction.NORTH), ifBranch -> { ... }, elseBranch -> { ... });
```

- An enum variable, node output, or expression can be on the right side. For example, `dir.isEqualTo(taskOutput)` is valid if the TaskDef returns a valid member of the `InlineEnumDef`.
- A right side whose type is `STR` *and* is not a literal is not accepted. For example, a `STR` variable or a TaskDef that returns `STR`. The server cannot validate this at registration time. To compare an enum with a `STR` variable, compare the member name: `dir.name().isEqualTo(strVariable)`.

## CLI Support

### `lhctl`

Enums are accepted on the CLI as case-sensitive member names:

```bash
lhctl run order-workflow direction NORTH
```

Something like this would be rejected as an invalid member:

```bash
lhctl run order-workflow direction north
lhctl run order-workflow direction GARBAGE
```


## Alternatives Considered

> I am leaving this for future reading, but `EnumDef`s are no longer being considered as a part of this proposal.

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

- **Dashboard.** Once `Enum` is implemented, we can render dropdown menus for Workflow executions.
- **UserTask fields.** Enum UserTask fields need `UserTaskField` to move from `VariableType` to `TypeDefinition`. Then, we can use `InlineEnumDef`s to render custom dropdown menus.