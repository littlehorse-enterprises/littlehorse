# Proposal: Type Safe Field Access in `WfSpec`s

- [Proposal: Type Safe Field Access in `WfSpec`s](#proposal-type-safe-field-access-in-wfspecs)
  - [Background](#background)
    - [`JSON_OBJ` vs `StructDef`/`Struct`](#json_obj-vs-structdefstruct)
    - [`JSON_ARR` vs `InlineArrayDef`/`Array`](#json_arr-vs-inlinearraydefarray)
  - [Client-Side Changes](#client-side-changes)
    - [The `get()` Method](#the-get-method)
    - [Method Chaining](#method-chaining)
  - [Protobuf Changes](#protobuf-changes)
    - [`StructPath` Type](#structpath-type)
      - [`StructPath` Example](#structpath-example)
    - [`VariableAssignment` Changes](#variableassignment-changes)
    - [`VariableMutation` Changes](#variablemutation-changes)
    - [`JsonIndex` Changes](#jsonindex-changes)
  - [Server-Side Changes](#server-side-changes)
  - [Overview](#overview)

Author: Jacob Snarr

This proposal will introduce a `get()` method for accessing fields from `Struct` and `Array` variables within `WfSpec`s. This new `get()` method will be backwards compatible with the `JSON_OBJ` and `JSON_ARR` types. Behind this new method, we will also introduce type safe field access for `Struct`s and `Array`s, ensuring that the field accessed exists and matches your source/destination type.

## Background

After the implementation of Proposal #000 "`StructDef` and `Struct`", we now have strongly typed structures in LittleHorse.

### `JSON_OBJ` vs `StructDef`/`Struct`

With the `JSON_OBJ` primitive type, we support loosely typed structures that can take on any form. This means that we can't guarantee the structure of a `JSON_OBJ` when compiling a `WfSpec`—only at runtime do we know what keys will be inside the `JSON_OBJ`.

`StructDef`s give the LittleHorse Server a rigid structure to validate `Struct` values against. If a `Struct` value doesn't match a `StructDef`'s format, the server rejects the `Struct` as soon as it reaches the server. This means any `Struct` that makes it into a workflow will be guaranteed to have all of the fields its `StructDef` requires. We can leverage this guarantee to provide compile-time type safety when accessing `Struct` fields in a `WfSpec`.

### `JSON_ARR` vs `InlineArrayDef`/`Array`

With the `JSON_ARR` primitive type, we support loosely typed arrays where each element can be a different type. This means that we can't guarantee the type of a `JSON_ARR` item when compiling a `WfSpec`.

`InlineArrayDef`s give the LittleHorse Server native Array support and the ability to constrain Array items to a single type. We can leverage this to provide compile-time type safety when accessing `Array` items in `WfSpec`.

## Client-Side Changes

### The `get()` Method

We will introduce a `WfRunVariable#get()` method in `WfSpec`s for accessing items within `Struct` and `Array` variables.

Take the following `WfSpec` for example, where we access the `"name"` field on a `Struct` variable:

```java
WfRunVariable customer = wf.declareStruct("input-customer", Customer.class);
wf.execute("greet", customer.get("name"));
```

Take this other `WfSpec`, where we greet the first customer in an Array of `Customers`s:

```java
WfRunVariable customers = wf.declareStruct("input-customers", Customer[].class);
wf.execute("greet", customer.get(0).get("name"));
```

### Method Chaining

This new `get()` method will be designed to support method chaining, meaning you can call it multiple times to get fields from nested `Struct`s or `Array`s:

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

#### `StructPath` Example

Take a look at how chaining multiple `get()` methods resolves into a `StructPath`:

**Java Code**
```java
WfRunVariable inputPerson = wf.declareStruct("input-person", Person.class);
wf.execute("greet", inputPerson.get("address").get("city"));
```

**ProtoJSON**
```json
"task": {
  "taskDefId": {
    "name": "greet"
  },
  "timeoutSeconds": 0,
  "retries": 0,
  "variables": [{
    "struct_path": {
      "path": ["address", "city"]
    },
    "variableName": "person"
  }]
}
```

This new type will be used in all messages where `json_path` was previously used:

### `VariableAssignment` Changes

We will add `StructPath` to a oneof for the `path`. We will deprecate the `json_path` option but preserve it for backwards compatibility.

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

### `VariableMutation` Changes

We will add `StructPath` as as a oneof for the `lhs_path` and the `NodeOutputSource` `path`. We will deprecate the `json_path` options but preserve them for backwards compatibility.

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

### `JsonIndex` Changes

We will change the name of `JsonIndex` to `Index` and add support for `StructPath`s and `TypeDefinition`s. We will deprecate the `field_path` JSONPath support but preserve the field for backwards compatibility.

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