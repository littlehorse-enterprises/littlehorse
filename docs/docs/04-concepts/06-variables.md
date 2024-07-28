---
sidebar_label: Variables
---

# `VariableDef` and `Variable`

A `Variable` in LittleHorse serves the same purpose as a variable in programming: it is a placeholder for a value that can be used in computation later. A `VariableDef` in LittleHorse defines a `Variable`. `VariableDef`s are mainly used in two places:

* To define input variables in a `TaskDef`.
* To define variables used in a `ThreadSpec` (part of a `WfSpec`).

## In the API

A `Variable` is a searchable object in the LittleHorse API. It contains the name and value of a specific variable instance in a `ThreadRun`.

The `Variable has a composite ID defined as follows:

1. The `wfRunId`, which is the ID of the associated `WfRun`.
2. The `threadRunNumber`, which is the ID of the associated `ThreadRun` (since a `Variable` lives within a specific `ThreadRun`).
3. The `name`, which is the name of the `Variable`.

The `Variable` object has a `name`, `type`, and a `VariableValue`.

:::info
Note that a `VariableDef` in itself is not a get-able object in the LittleHorse API; it is a sub-structure of other objects (generally `TaskDef` and `WfSpec`).
:::

## Variable Types

LittleHorse currently supports variables of the following types:

#### `INT`

The `INT` variable type is stored as a 64-bit integer. The `INT` can be cast to a `DOUBLE`.

#### `DOUBLE`

The `DOUBLE` variable type is a 64-bit floating point number. It can be cast to an `INT`.

#### `STR`

The `STR` variable type is stored as a String. `INT`, `DOUBLE`, and `BOOL` variables can be cast to a `STR`.

#### `BOOL`

A `BOOL` is a simple boolean switch.

#### `JSON_OBJ`

The `JSON_OBJ` variable allows you to store complex objects in the JSON format. When using the Java and GoLang SDK's, the `JSON_OBJ` variable type is often used transparently to the user. For example, the Java Task Worker SDK can inspect your method signature and automatically deserialize an input variable into a POJO.

#### `JSON_ARR`

The `JSON_ARR` variable allows you to store collections of objects as a JSON array. The behavior is similar to the `JSON_OBJ` variable type.

#### `BYTES`

The `BYTES` variable type allows you to store an arbitrary byte string.

#### `NULL`

The `NULL` variable type is used for `Node`s that have no output, and for `Variable`s that have not yet been initialized (for example, if their value is not provided when starting the `ThreadRun`).

## Using Varibles

:::note
The `VariableAssignment` protobuf structure described in this section applies to the JSON `WfSpec` specification. The SDK's (eg. Java, Go, Python) abstract away the `VariableAssignment` when authoring a `WfSpec`. Nevertheless, it is useful to understand how it works.
:::

Recall that a `TASK` node takes in multiple input variables. In the raw JSON `WfSpec`, you do this via what's called a `VariableAssignment`. The `VariableAssignment` is much the same as passing an argument to a function call in a programming language.

A `VariableAssignment` can specify the Variable Value to be used in one of the following three ways:

- Pass in a literal value (eg. a `STR` or an `INT`).
- Use a value from a `Variable` in your workflow.
- A `format_string`, which takes in a raw string and then a list of `VariableAssignment`s to fill in any parameters.

`VariableAssignment`s are used in several places, including:

- Passing inputs to a `TASK` node.
- Acting as the left-hand-side and right-hand-side for conditional expressions.
- Acting as the right-hand-side for variable mutations.

## Mutating Variables

Upon the completion of any `NodeRun` (no matter what type of `Node`), you may specify a list of Variable Mutations for LittleHorse to execute. A `VariableMutation` requires the following information:

- The name of the `Variable` to mutate (LHS).
- The type of mutation.
- The "right-hand-side" of the mutation (RHS).

We will unpack each of those sections in more detail below.

### Selecting the Variable to Mutate

This is relatively straight forward--you only need to provide the `name` of the `Variable` that you wish to mutate. The named variable could belong to a parent (or grandparent) `ThreadRun`; the effect is the same.

For `JSON_OBJ` and `JSON_ARR` `Variable`s, you can mutate a sub-object of the `Variable` by specifying a `jsonPath`.

