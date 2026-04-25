# Building `Struct`s in the DSL

We want to be able to build `Struct` values inside the `WfSpec` DSL rather than only mutate existing values.

## Motivation

Consider the simple `user` `StructDef` here with two required fields:

```java
class User {
    public String email;
    public String fullName;
}
```

The following code fails with the littlehorse equivalent of a "null pointer exception": `Cannot assign STR to null without explicit casting.`

```java
WfRunVariable email = wf.declareStr("email").required(); // input var
WfRunVariable userRecord = wf.declareStruct("user", User.class); // Internal Struct

NodeOutput fullName = wf.execute("fetch-full-name", email);

// Now try to "assemble" the Struct
userRecord.get("fullName").assign(fullName);
userRecord.get("email").assign(email);

// Now use the Struct
wf.execute("save-user", userRecord);
```

That is expected. It's the same as the following plain java code:

```java
void foo(String email) {
    User user = null; // not passed in.

    user.setEmail(email);
    user.setFullName(fetchFullName(email));

    saveRecord(user);
}
```

## Proposal

What we need to do is allow users to create an `InlineStruct` and initialize the required values in a single `VariableMutation`.

### Java SDK Design

The SDK exposes two separate builder types:

- **`LHStructBuilder`** — for named Struct values backed by a `StructDef`. Extends `Serializable`, so it can be passed as a task argument, assigned to a variable, or used anywhere a `Serializable` is expected.
- **`InlineLHStructBuilder`** — for nested inline sub-structures. Does **not** extend `Serializable`, so it can only appear as a field value inside another builder (via an overloaded `put` method). This prevents accidentally passing an inline builder to `execute()`, `assign()`, or any other API that accepts `Serializable`.

Version pinning is done at construction time via an overloaded `buildStruct` factory method rather than a fluent `withVersion()` call, making the version immutable once the builder is created.

### Java SDK Examples

Here's how it will look in the Java SDK.

#### Basic

```java
WfRunVariable email = wf.declareStr("email").required(); // input var
WfRunVariable userRecord = wf.declareStruct("user", User.class); // Internal Struct

NodeOutput fullName = wf.execute("fetch-full-name", email);

// Uses latest StructDef version
LHStructBuilder struct = wf.buildStruct("user")
        .put("email", email)
        .put("fullName", fullName);

userRecord.assign(struct);

wf.execute("save-user", userRecord);
```

#### Pinned Version

```java
// Pins to StructDef version 3
LHStructBuilder struct = wf.buildStruct("user", 3)
        .put("email", email)
        .put("fullName", fullName);
```

#### Nested Structs

Let's say we have the following `StructDef` class:

```java
class Address {
  public String streetAddress;
  public String state;
  public int zip;
}

@LHStructDef("person")
class Person {
  public String name;
  public Address address;
}
```

We'd need to allow doing the following:

```java
WfRunVariable name = wf.declareStr("name").required(); // input var
WfRunVariable personRecord = wf.declareStruct("my-person", Person.class);

NodeOutput addressJson = wf.execute("fetch-address-json", name);

LHStructBuilder personStruct = wf.buildStruct("person")
    .put("name", name)
    .put("address", wf.buildInlineStruct()
          .put("streetAddress", addressJson.jsonPath("$."))
          .put("state", addressJson.jsonPath("$.state"))
          .put("zip", addressJson.jsonPath("$.zip")));

personRecord.assign(personStruct);
```

Note that `wf.buildInlineStruct()` returns `InlineLHStructBuilder`, which is accepted by the `put(String, InlineLHStructBuilder)` overload on `LHStructBuilder`. Attempting to pass the inline builder directly to `wf.execute(...)` or `variable.assign(...)` would be a compile error since `InlineLHStructBuilder` is not `Serializable`.

### Protobuf

We will add a new entry to the `oneof` in the `oneof source`.

```protobuf
message VariableAssignment {
  // ...

  oneof source {
    // ...

    // Builds a Struct.
    StructBuilder struct_builder = 8;
  }
}

// Builds a Struct using data available in the context of this ThreadRun.
message StructBuilder {
  // The ID of the StructDef we're building. If the version is -1, it uses the latest.
  StructDefId struct_def_id = 1;

  // Determines the content of the InlineStruct.
  InlineStructBuilder value = 2;
}

// Builds an InlineStruct from values available to a ThreadRun.
message InlineStructBuilder {
  // Determines the values for each field in the resulting InlineStruct.
  map<string, InlineStructFieldValue> fields = 1;
}

message InlineStructFieldValue {
  // Determines the Source value for this field.
  oneof struct_value {
    // Simple value that already exists in the context of the ThreadRun.
    VariableAssignment simple_value = 1;

    // Builds a nested sub-structure of a Struct
    InlineStructBuilder sub_structure = 2;
  }
}

```

Editing the `VariableAssignment` keeps this as flexible as possible, and with minimal proto & code changes.

### Server-Side Validations

The `VariableAssignmentModel#resolveType()` will return a `TypeDefinitionModel` of type `StructDef` with an appropriate `StructDefId`, which will automatically handle validations for any usage of this `VariableAssignment`.

When validating the `VariableAssignment` itself (inside `VariableAssignmentModel#validate()`), we will:

1. Ensure that every field assignment is compatible with the type from the `StructDef` that it's used for.
2. Ensure that every non-nullable field without a default is provided in the `fields`.

## Rejected Alternatives

### Empty `Struct` Initialization

ChatGPT came up with an idea of initializing any `Struct` variable to not be NULL. However, this breaks the idea that, for a well-formed `Struct`, all non-nullable fields that do not have defaults are set.

### Use `InlineStruct` not `Struct`

I thought about not specifying the `StructDef` we're building for. This would allow for a lot of flexible usage. However, that defeats the purpose of `Struct`s as they are suddenly no longer compatible with the type safety of structs.

### Single `LHStructBuilder` Interface with `withVersion()`

An earlier design had a single `LHStructBuilder` interface (extending `Serializable`) shared by both named and inline builders, with a `withVersion(int)` method for version pinning. This was rejected because:

1. **No compile-time safety** — an inline builder could accidentally be passed to `execute()` or `assign()` since it was `Serializable`. The `inlineOnly` flag only caught misuse at runtime.
2. **`withVersion()` leaked named-builder semantics** — it was only valid on named builders, and inline builders had to throw at runtime if called. Moving version to the constructor makes it immutable and removes the invalid API surface.
