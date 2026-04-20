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

### Java SDK Examples

```java
WfRunVariable email = wf.declareStr("email").required(); // input var
WfRunVariable userRecord = wf.declareStruct("user", User.class); // Internal Struct

NodeOutput fullName = wf.execute("fetch-full-name", email);

LHStructBuilder struct = wf.buildStruct("user")
        .withVersion(3) // optional
        .put("email", email)
        .put("fullName", fullName);

userRecord.assign(struct);

wf.execute("save-user", userRecord);
```

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

  // Builds an InlineStruct using data available in the context of this ThreadRun.
  message StructBuilder {
    // Sets the value for the fields of each 
    map<string, VariableAssignment> fields = 1;

    // The ID of the StructDef we're building. If the version is -1, it uses the latest.
    StructDefId struct_def_id = 2;
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
