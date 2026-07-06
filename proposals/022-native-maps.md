# Proposal: Native `Map`s

- [Proposal: Native `Map`s](#proposal-native-maps)
  - [Background](#background)
    - [The Problems with JSON Maps](#the-problems-with-json-maps)
  - [API Glossary](#api-glossary)
  - [Protobuf Changes](#protobuf-changes)
    - [`TypeDefinition` Changes (`InlineMapDef`)](#typedefinition-changes-inlinemapdef)
    - [`VariableValue` Changes (`Map`)](#variablevalue-changes-map)
    - [Allowed Key Types](#allowed-key-types)
  - [Client-Side (SDK) Changes](#client-side-sdk-changes)
    - [Declaring a `Map` Variable](#declaring-a-map-variable)
    - [Accessing Entries](#accessing-entries)
    - [Type Inference from Native Language Maps](#type-inference-from-native-language-maps)
    - [Distinguishing native Maps from JSON\_OBJ](#distinguishing-native-maps-from-json_obj)
  - [Server-Side Changes](#server-side-changes)
    - [Type Validation](#type-validation)
    - [Mutations](#mutations)
    - [Path Access](#path-access)
    - [Casting](#casting)
    - [Comparators and Indexing](#comparators-and-indexing)
  - [Backwards Compatibility](#backwards-compatibility)

Author: Jacob Snarr

This proposal will cover the following:

- Introduce a new strongly-typed `Map` value to LittleHorse, representing a mapping from a key `VariableValue` to a value `VariableValue`.
- Introduce a new `TypeDefinition` case, `InlineMapDef`, that defines the key and value types of a `Map`.

## Background

LittleHorse has been moving towards strong typing (see [Proposal 002](./002-move-to-strong-typing.md)). We already have strongly-typed collections via the `Array` value and the `InlineArrayDef` `TypeDefinition` case (see [Proposal 001](./000-struct-and-structdef.md)), and strongly-typed records via the `Struct` value and `StructDef`/`InlineStructDef`. The last common type that is missing is a Map type.

### The Problems with JSON Maps

Currently, the only way to model a map in a `WfSpec` is to use a `JSON_OBJ`. This has the same fundamental problems described in [Proposal 011](./011-type-safe-structpaths.md):

- The structure is untyped: we cannot validate the key or value types at registration time.
- Keys are restricted to strings (a consequence of JSON object semantics), so there is no first-class way to key a collection by an `INT`, `WF_RUN_ID`, or `TIMESTAMP`.
- Server-side indexing, casting, and comparison cannot reason about the contents.

A native `Map` value solves these problems by carrying an authoritative `InlineMapDef` describing the key and value types, exactly as `Array` carries an `InlineArrayDef` element type.

## API Glossary

This adds one new value type to LittleHorse (defined in the [`schemas`](../schemas) folder):

- **`Map`**: a `VariableValue` representing a mapping from a key `VariableValue` to a value `VariableValue`. The key type and value type are described by an `InlineMapDef`.

And one new `TypeDefinition` case:

- **`InlineMapDef`**: an inline schema definition for a native LittleHorse `Map`. It contains a `key_type` `TypeDefinition` and a `value_type` `TypeDefinition`.

## Protobuf Changes

### `TypeDefinition` Changes (`InlineMapDef`)

We add a new case to the `defined_type` oneof in [`type_definition.proto`](../schemas/littlehorse/type_definition.proto), mirroring `InlineArrayDef`:

```proto
message TypeDefinition {
  oneof defined_type {
    VariableType primitive_type = 1;
    StructDefId struct_def_id = 5;
    InlineArrayDef inline_array_def = 6;

    // An inline Map definition.
    InlineMapDef inline_map_def = 7;
  }

  reserved 2, 3;

  bool masked = 4;
}

// Inline schema definition for a native LittleHorse Map.
message InlineMapDef {
  // Type definition for each key in the map.
  TypeDefinition key_type = 1;

  // Type definition for each value in the map.
  TypeDefinition value_type = 2;
}
```

Because `key_type` and `value_type` are themselves `TypeDefinition`s, `Map`s nest naturally: e.g. `Map<STR, Array<INT>>` or `Map<INT, Struct<Customer>>`. (Constraints on key types are discussed [below](#allowed-key-types).)

### `VariableValue` Changes (`Map`)

We add a new case to the `value` oneof in [`variable.proto`](../schemas/littlehorse/variable.proto), mirroring `Array`:

```proto
message VariableValue {
  reserved 1;

  oneof value {
    string json_obj = 2;
    string json_arr = 3;
    double double = 4;
    bool bool = 5;
    string str = 6;
    int64 int = 7;
    bytes bytes = 8;
    WfRunId wf_run_id = 9;
    google.protobuf.Timestamp utc_timestamp = 10;
    Struct struct = 11;
    Array array = 12;

    Map map = 13;
  }
}

// A Map is a strongly-typed mapping from key values to value values.
message Map {
  // A single key/value entry in the Map.
  message Entry {
    VariableValue key = 1;
    VariableValue value = 2;
  }

  // The entries of the map.
  //
  // NOTE: We use a repeated list of entries rather than a protobuf `map<...>` because
  // protobuf only permits scalar (string/integer/bool) keys, whereas a LittleHorse
  // `Map` key is a full `VariableValue` (which may be a `WF_RUN_ID`, `TIMESTAMP`, etc.).
  repeated Entry entries = 1;

  // Optional, authoritative key/value types for this map. Stored alongside the
  // entries for ease of access on the server, mirroring `Array.element_type`.
  // If absent, the types may be unknown and must be derived from entries or
  // treated as wildcard.
  optional InlineMapDef map_type = 2;
}
```

Note the choice not to use protobuf's native `map<...>`: a protobuf map key must be an integral or string scalar, while a LittleHorse `Map` key is an arbitrary `VariableValue`. Using a `repeated Entry` keeps keys fully expressive and keeps wire-level ordering deterministic.

### Allowed Key Types

To keep `Map` semantics well-defined (uniqueness and equality of keys), keys are restricted to primitive `VariableType`s. `Struct`, `Array`, and `Map` keys are not permitted because evaluating the deep equality of complex key types can be expensive and error-prone.

`value_type` has no such restriction: values may be any `TypeDefinition`, including nested `Struct`s, `Array`s, or `Map`s.

The server rejects a `WfSpec`, `TaskDef`, `StructDef`, or any other metadata object whose `InlineMapDef` declares a non-primitive `key_type`, returning an `INVALID_ARGUMENT`/validation error at registration time.

## Client-Side (SDK) Changes

### Declaring a `Map` Variable

We will add a way to declare a `Map`-typed `WfRunVariable`, parallel to the existing `Array` and `Struct` helpers. Consistent with the rest of the `WfSpec` DSL, the SDK accepts **language-native types** rather than `VariableType` enum values — the SDK maps each native type onto the appropriate `TypeDefinition`. For example, in the Java SDK:

```java
// A Map from STR keys to INT values.
WfRunVariable wordCounts = wf.declareMap("word-counts", String.class, Integer.class);
```

Because the key and value are expressed as native types, the same API naturally supports `StructDef`-backed value types. A `Map` whose values are `Customer` `Struct`s is declared by passing the POJO class, exactly as `declareStruct` does:

```java
// A Map from STR keys to Customer Struct values.
WfRunVariable customersById = wf.declareMap("customers-by-id", String.class, Customer.class);
```

The SDK resolves `Customer.class` into the corresponding `StructDefId` and sets it as the `value_type` of the `InlineMapDef`. Keys remain restricted to types that resolve to a primitive `VariableType` (see [Allowed Key Types](#allowed-key-types)); passing a `Struct`/collection class as the key type is rejected by the SDK.

### Accessing Entries

`Map`s integrate with the type-safe `get()` access introduced in [Proposal 011](./011-type-safe-structpaths.md). A `field_name`/key selector resolves a value out of a `Map`:

```java
WfRunVariable wordCounts = wf.declareMap("word-counts", String.class, Integer.class);

// Resolves the INT value associated with key "hello".
wf.execute("report", wordCounts.get("hello"));
```

At runtime, accessing a key that is not present resolves to `NULL` (consistent with how missing `Struct` fields behave), unless type-safety validation determines otherwise.

### Type Inference from Native Language Maps

When a `Map` value is passed as input (e.g. as a `WfRun` variable or task output), the SDKs infer the `InlineMapDef` from the native language type where possible:

**Ideal Case**:
- **Java**: a `java.util.Map<K, V>` infers key/value types from `K`/`V`.
- **Python**: a `dict` infers types from its annotations or homogeneous contents.
- **Go / C# / TypeScript**: analogous inference from the native map/dictionary type.

### Distinguishing native Maps from JSON_OBJ

At the Task Worker level, we already support Maps when serializing to and from `JSON_OBJ`. Trying to support native Maps means that we will now support two different LittleHorse native types that translate to one target host language type, i.e. "Map" in Java.

We will use a new annotation boolean field `@LHType(isLHMap=true)` to indicate whether a certain part of a Task Method should be considered a native Map. Users can use this annotation on the Task Method to annotate the return type, or on a Task Parameter to annotate the input type.

```java
@LHTaskMethod("my-task")
@LHType(isLHMap = true)
public Map<String, Integer> myTask(
        @LHType(isLHMap = true) Map<String, Integer> input) {
    // ...
}
```

## Server-Side Changes

### Type Validation

When a metadata object referencing an `InlineMapDef` is registered, the server validates that:

1. `key_type` is a primitive `VariableType` (see [Allowed Key Types](#allowed-key-types)).
2. `value_type` is a valid `TypeDefinition`.
3. Neither `key_type` nor `value_type` use forbidden JSON primitives (`JSON_OBJ`/`JSON_ARR`), consistent with `InlineArrayDef` validation.
4. If either type references a `StructDef`, the `StructDef` must exist and its version is pinned.

At ingress points (`RunWf`, task output assignment, `ThrowEvent`), the server stamps the `InlineMapDef` onto the `MapModel` (via `IngressTypeUtils`), then validates every `Entry`'s key against `key_type` and every value against `value_type`, failing with a `TypeValidationException` on mismatch (consistent with `Array`/`Struct` handling).

### Mutations

The following `VariableMutationType` operations are supported on `Map` values:

- **`ASSIGN`**: replace the entire map.
- **`EXTEND`**: combines two maps. The RHS must be a `Map`. If a key already exists in the LHS map, the value is replaced with the value from the RHS map; otherwise, every entry is appended.
- **`REMOVE_KEY`**: remove an entry by key. The RHS must be compatible with the map's key type.

Operations that do not apply (`ADD`, `SUBTRACT`, `MULTIPLY`, `DIVIDE`, `POW`, `REMOVE_IF_PRESENT`, `REMOVE_INDEX`) throw descriptive errors.

### Path Access

A `Map` integrates with `LHPath` via the `KEY` selector. Given a `Map<STR, INT>` variable `wordCounts`, the path `.get("hello")` resolves to the `INT` value associated with key `"hello"`. At the type level, `getNestedType()` returns the map's `value_type` when it encounters a `KEY` selector on an `INLINE_MAP_DEF`.

### Casting

A `Map` cannot be cast to a `JSON_OBJ` but a `JSON_OBJ` should be castable to a `Map`. This is because the LittleHorse native typing system supports a superset of JSON types, meaning we can't cast from a LittleHorse structure to a JSON structure without losing some data. This is evident when looking at unique LittleHorse types like `WF_RUN_ID` and `TIMESTAMP`.

### Comparators and Indexing

- `EQUALS` / `NOT_EQUALS` perform proto-level equality comparison.
- `IN` / `NOT_IN` (`CONTAINS`) checks whether the RHS key exists in the map's key set.

## Backwards Compatibility

This change is fully backwards compatible. It only adds new protobuf features and fields, and does not affect old ones. It also adds new functionality to the SDKs for distinguishing InlineMapDefs/Maps from JSON_OBJs, and none of that functionality breaks old clients who depend on JSON_OBJs.
