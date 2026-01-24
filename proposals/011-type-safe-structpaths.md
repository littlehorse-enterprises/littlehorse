# Proposal: Type Safe Field Access in `WfSpec`s

- [Proposal: Type Safe Field Access in `WfSpec`s](#proposal-type-safe-field-access-in-wfspecs)
  - [Background](#background)
    - [The Problems with JSON](#the-problems-with-json)
    - [The Solution to JSON and JSONPath](#the-solution-to-json-and-jsonpath)
  - [Client-Side Changes](#client-side-changes)
    - [The `get()` Method](#the-get-method)
    - [Method Chaining](#method-chaining)
  - [Protobuf Changes](#protobuf-changes)
    - [`LHPath` Type](#lhpath-type)
      - [`LHPath` Example](#lhpath-example)
    - [`VariableAssignment` Changes](#variableassignment-changes)
    - [`VariableMutation` Changes](#variablemutation-changes)
    - [`JsonIndex` Changes](#jsonindex-changes)
  - [Server-Side Changes](#server-side-changes)
  - [Overview](#overview)
  - [Alternatives](#alternatives)
    - [(Server): Adapt JSONPath to support `Struct`s and `Array`s](#server-adapt-jsonpath-to-support-structs-and-arrays)
    - [(Server+Clients)): Also translate `get()` methods to `jsonPath`](#serverclients-also-translate-get-methods-to-jsonpath)
    - [Analysis](#analysis)

Author: Jacob Snarr

This proposal will cover the following:

- Introduce a method for accessing `Struct` fields and `Array` items in the `WfSpec` DSL
- Cover type safety mechanisms to validate the use of `Struct` fields and `Array` items at runtime

## Background

### The Problems with JSON

LittleHorse provides two primitive types, `JSON_OBJ` and `JSON_ARR`, for storing complex data. Both of these primitive types are loosely structured, so we can't predict their structure at "compile-time", only at "runtime".
We do provide a mechanism for accessing and modifying parts of these structures within `WfSpec`s: the `jsonPath` method. The `jsonPath` method allows users to select data from `JSON_OBJ` and `JSON_ARR`s within `WfSpec`s like so:

```java
// Workflow takes in a "person" `JSON_OBJ`
WfRunVariable person = wf.declareJsonObj("person").required();

// Passes just the "name" field into the "greet" task.
wf.execute("greet", person.jsonPath("$.name"));
```

Once again, we don't know the structure of the `JSON_OBJ` or a `JSON_ARR` until runtime—if the field "name" doesn't exist in the example above, the `WfRun` will fail at runtime.

This is **the core problem with JSON** that `StructDef`s and `Struct`s set out to solve: provide reliable schemas to catch malformed data before it enters the server. 

### The Solution to JSON and JSONPath

In the following section, we will introduce an alternative to `jsonPath` that will work with `Struct`s and `Arrays`. Once we've built an alternative to `jsonPath`, we can start building type safety mechanisms to catch missing fields or mismatched types at runtime.

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

### `LHPath` Type

To enable this new functionality, we will introduce a new `LHPath` type for extracting data from `Struct`s, `Arrays`, `JSON_OBJ`, and `JSON_ARR`s:

```proto
// A path of repeated Strings resolving to a nested field in an object.
message LHPath {
  message Selector {
    oneof type {
      string field_name = 1;
      int32 array_index = 2;

      // Can add support for other selectors in the future, like ranges
    }
  }

  repeated Selector path = 1;
}
```

#### `LHPath` Example

Take a look at how chaining multiple `get()` methods resolves into a `LHPath`:

**Java Code**
```java
WfRunVariable inputPerson = wf.declareStruct("input-person", Person.class);
wf.execute("greet", inputPerson.get("friends").get(0));
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
    "lhPath": {
      "path": [
        {
          "fieldName": "address"
        },
        {
          "arrayIndex": 0
        }
      ]
    },
    "variableName": "person"
  }]
}
```

This new type will be used in all messages where `json_path` was previously used:

### `VariableAssignment` Changes

We will add `LHPath` to a oneof for the `path`. We will deprecate the `json_path` option but preserve it for backwards compatibility.

```proto
message VariableAssignment {
  // ...

  // If you provide a `variable_name` and the specified variable is a JSON_OBJ, JSON_ARR, or
  // StructDef type, then you may provide a `path` for querying data within your object.
  oneof path {
    // DEPRECATED: A String path formatted in the `JSONPath` format.
    string json_path = 1 [deprecated=true];

    // A path resolving to a field in your object.
    LHPath lh_path = 7;
  }

  // ...
}
```

### `VariableMutation` Changes

We will add `LHPath` as as a oneof for the `lhs_path` and the `NodeOutputSource` `path`. We will deprecate the `json_path` options but preserve them for backwards compatibility.

```proto
message VariableMutation {
  // Specifies to use the output of a NodeRun as the RHS.
  message NodeOutputSource {
    // Use this specific field from a Struct, Array, JSON_OBJ, or JSON_ARR output
    oneof path {
      // DEPRECATED: A String path formatted in the `JSONPath` format.
      string jsonpath = 10 [deprecated=true];

      // A path resolving to a field in your object.
      LHPath lh_path = 11;
    }
  }

  // For Struct, Array, JSON_ARR and JSON_OBJ variables, this allows you to optionally mutate
  // a specific sub-field of the variable.
  oneof lhs_path {
    // DEPRECATED: A String path formatted in the `JSONPath` format.
    string lhs_json_path = 2 [deprecated=true];

    // A path resolving to a field in your object.
    LHPath lhs_path = 7;
  }
}
```

### `JsonIndex` Changes

We will change the name of `JsonIndex` to `Index` and add support for `LHPath`s and `TypeDefinition`s. We will deprecate the `field_path` JSONPath support but preserve the field for backwards compatibility.

```proto
// Defines an index to make a Struct, Array, JSON_OBJ or JSON_ARR variable searchable over a path to a field.
message Index {
  oneof path {
    // Denotes the path in JSONPath format (according to the Java Jayway library) 
    string field_path = 1 [deprecated=true];

    // Denotes the path in LHPath format
    LHPath lh_path = 2;
  }

  // DEPRECATED: Is the type of the field we are indexing.
  VariableType field_type = 2 [deprecated=true];

  // The type of the field we are indexing.
  TypeDefinition field_type_def = 3;
}
```

## Server-Side Changes

In addition to adding the necessary code to make `LHPath`s behave just like `json_path`s, we will add the following type safety checks to the server whenever a `LHPath` is used:

1. Check that a `LHPath` Resolves

We will validate that a `LHPath` exists on a given `StructDef` whenever a `LHPath` enters the server. If the `LHPath` does not resolve to a field, we will return an error to the client explaining what happened.

1. Check that the `LHPath` Matches Type

When a `LHPath` resolves to the field of a `StructDef`, we will validate that the type of the `StructFieldDef` matches the destination of the data.

When a `LHPath` resolves to an `InlineArrayDef` index, we will validate that the type of the `InlineArrayDef` matches the destination of the data.

## Overview

With the introduction of `LHPath`s , we will add the following functionality to LittleHorse:

- Compile time type safety on `StructDef` and `InlineArrayDef` field access
- Support for method chaining to access fields on `JSON_OBJ`, `JSON_ARR`, `Struct` and `Array` variables
- Format-agnostic way for accessing fields from `JSON_OBJ` and `JSON_ARR`, an improvement upon `json_path` which depended on the `JSONPath` format of the Java Flyway library

We will deprecate `json_path` and dedicate all future `path` development towards `LHPath` functionality. We will maintain support for old WfSpecs that depend on `json_path`.

## Alternatives

This proposal paves the way for the full removal of our JSONPath support and introduces a more LittleHorse DSL friendly way for accessing fields from objects. But what if we didn't remove JSONPath at all, and instead adapted around it to support `Struct`s and `Array`s? Below you'll find a few ideas for doing so:

### (Server): Adapt JSONPath to support `Struct`s and `Array`s

The simplest alternative to this proposal would be adapting our existing JSONPath implementation to support `Structs` and `Array`s at the server level.

Pros:
- No changes at the SDK level
- No room for new SDK implementations to diverge
- Makes the transition from `JSON_OBJ` to `Struct`s easy for existing LH users

Cons:
- No support for method chaining
- Server stays dependent on the JSONPath format
- Introduces a lot of complexity at the server level as we have to adapt JSONPaths to access information from `Struct`s and `Array`s 

On paper, this option requires the least amount of changes for adding type safe field access to `Struct`s and `Array`s. It also provides the least amount of UX improvements for users, as it maintains a full dependency on `jsonPath` and requires users to research and understand the JSONPath format for working with `Struct`s and `Arrays`. 

However, the JSONPath specification includes a lot of advanced options for querying data from JSON objects, such as filters and regular expressions. Adapting JSONPath, especially the advanced options for querying data, to work with `Struct`s and `Array`s would be very diffuclt.

### (Server+Clients)): Also translate `get()` methods to `jsonPath`

If we do manage to adapt JSONPath to work with `Struct`s and `Array`s, we could enhance the UX of the SDK by designing a `get()` method that translates to JSONPath under the hood.

Pros:
- Translating chained `get()` methods to JSONPath is trivial, as `jsonPath` is not a hard format to follow.
- Reduces complexity in protobuf/server implementation
  
Cons:
- Introduces complexity to our SDKs
  - Various SDK impelementations could diverge if implemented incorrectly.
- Server stays dependent on JSONPath format
- Introduces a lot of complexity at the server level as we have to adapt JSONPaths to access information from `Struct`s and `Array`s 

### Analysis

Both of these alternatives reduce the complexity for adding type safe field access to `WfSpec`s by preserving our dependency on JSONPath. These alternatives hinge on the idea that we could adapt JSONPath to work with `Struct`s and `Array`s, which may be very difficult given the advanced options JSONPath provides for querying data.