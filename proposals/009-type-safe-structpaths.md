# Proposal: Type Safe `StructPath`s in `WfSpec`s

- [Proposal: Type Safe `StructPath`s in `WfSpec`s](#proposal-type-safe-structpaths-in-wfspecs)
  - [Background](#background)
  - [Client-Side Changes](#client-side-changes)
    - [The `get()` Method](#the-get-method)
    - [Method Chaining](#method-chaining)
  - [Protobuf Changes](#protobuf-changes)
    - [`StructPath` Type](#structpath-type)
    - [`VariableAssignment`](#variableassignment)
    - [`VariableMutation`](#variablemutation)
    - [`JsonIndex`](#jsonindex)
  - [Server-Side Changes](#server-side-changes)
  - [Overview](#overview)

Author: Jacob Snarr

This proposal will introduce a type safe way for accessing fields from `Struct` and `Array` variables within `WfSpec`s.

## Background

After the implementation of Proposal #000 "`StructDef` and `Struct`", we now have strongly typed structures in LittleHorse.

With the `JSON_OBJ` primitive type, we support loosely typed structures that can take on any form. This means that we can't guarantee the structure of a `JSON_OBJ` when compiling a `WfSpec`—only at runtime do we know what keys will be inside the `JSON_OBJ`.

`StructDef`s give the LittleHorse Server a rigid structure to validate `Struct` values against. If a `Struct` value doesn't match a `StructDef`'s format, the server rejects the `Struct` as soon as it reaches the server. This means any `Struct` that makes it into a workflow will be guaranteed to have all of the fields its `StructDef` requires. We can leverage this guarantee to provide compile-time type safety when accessing `Struct` fields in a `WfSpec`.

## Client-Side Changes

### The `get()` Method

We will introduce a `WfRunVariable#get()` method in `WfSpec`s for accessing fields within a `Struct` variable.

Take the following `WfSpec` for example, where we access the `"name"` field on a `Struct` variable:

```java
WfRunVariable inputPerson = wf.declareStruct("input-person", Person.class);
wf.execute("greet", inputPerson.get("name"));
```

### Method Chaining

This new `get()` method will be designed to support method chaining, meaning you can call it multiple times to get fields from nested `Struct`s:

```java
WfRunVariable inputPerson = wf.declareStruct("input-person", Person.class);
wf.execute("greet", inputPerson.get("address").get("city"));
```

## Protobuf Changes

### `StructPath` Type

To enable this new functionality, we will introduce a new `StructPath` type for extracting data from `Struct`s, `Arrays`, `JSON_OBJ`, and `JSON_ARR`s:

```proto
// A path of repeated Strings resolving to a nested field in an object.
message StructPath {
  repeated string path = 1;
}
```

This new type will be used in all messages where `json_path` was previously used:

### `VariableAssignment`

```proto
message VariableAssignment {
  // ...

  // If you provide a `variable_name` and the specified variable is a JSON_OBJ, JSON_ARR, or
  // StructDef type, then you may provide a `path` for querying data within your object.
  oneof path {
    // DEPRECATED: A String path formatted in the `JSONPath` format.
    string json_path = 1 [deprecated=true];

    // A StructPath resolving to a field in your object.
    StructPath struct_path = 7;
  }

  // ...
}
```

### `VariableMutation`

```proto
message VariableMutation {
  // Specifies to use the output of a NodeRun as the RHS.
  message NodeOutputSource {
    // Use this specific field from a Struct, Array, JSON_OBJ, or JSON_ARR output
    oneof path {
      // DEPRECATED: A String path formatted in the `JSONPath` format.
      string jsonpath = 10 [deprecated=true];

      // A StructPath resolving to a field in your object.
      StructPath struct_path = 11;
    }
  }

  // For Struct, Array, JSON_ARR and JSON_OBJ variables, this allows you to optionally mutate
  // a specific sub-field of the variable.
  oneof lhs_path {
    // DEPRECATED: A String path formatted in the `JSONPath` format.
    string lhs_json_path = 2 [deprecated=true];

    // A StructPath resolving to a field in your object.
    StructPath lhs_struct_path = 7;
  }
}
```

### `JsonIndex`

We will change the name of `JsonIndex` to `Index` and add support for `StructPath`s and `TypeDefinition`s:

```proto
// Defines an index to make a Struct, Array, JSON_OBJ or JSON_ARR variable searchable over a path to a field.
message Index {
  oneof path {
    // Denotes the path in JSONPath format (according to the Java Jayway library) 
    string field_path = 1 [deprecated=true];

    // Denotes the path in StructPath format
    StructPath struct_path = 2;
  }

  // DEPRECATED: Is the type of the field we are indexing.
  VariableType field_type = 2 [deprecated=true];

  // The type of the field we are indexing.
  TypeDefinition field_type_def = 3;
}
```

## Server-Side Changes

In addition to adding the necessary code to make `StructPath`s behave just like `json_path`s, we will add the following type safety checks to the server whenever a `StructPath` is used:

1. Check that a `StructPath` Resolves

We will validate that a `StructPath` exists on a given `StructDef` whenever a `StructPath` enters the server. If the `StructPath` does not exist, we will return an error to the client explaining what happened.

2. Check that the `StructPath` Matches Type

When a `StructPath` resolves to the field of a `StructDef`, we will validate that the type of the `StructFieldDef` matches the source or destination of the data.

When a `StructPath` resolves to an `InlineArrayDef` index, we will validate that the type of the `InlineArrayDef` matches the source/destination of the data.

## Overview

With the introduction of `StructPath`s , we will add the following functionality to LittleHorse:

- Compile time type safety on `StructDef` and `InlineArrayDef` field access
- Support for method chaining to access fields on `JSON_OBJ`, `JSON_ARR`, `Struct` and `Array` variables
- Format-agnostic way for accessing fields from `JSON_OBJ` and `JSON_ARR`, an improvement upon `json_path` which depended on the `JSONPath` format of the Java Flyway library

We will deprecate `json_path` and dedicate all future `path` development towards `StructPath` functionality. We will maintain support for old WfSpecs that depend on `json_path`.