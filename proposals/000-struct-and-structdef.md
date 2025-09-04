# Proposal: `StructDef` and `Struct`

- [Proposal: `StructDef` and `Struct`](#proposal-structdef-and-struct)
  - [Server-Side Changes](#server-side-changes)
    - [`Struct` and `StructDef` Protobuf](#struct-and-structdef-protobuf)
      - [Inline Structs](#inline-structs)
      - [`StructDef` Versioning](#structdef-versioning)
    - [Existing Protobuf](#existing-protobuf)
    - [`StructDef` Schema Evolution](#structdef-schema-evolution)
      - [Validating `StructDef` Schema Evolution](#validating-structdef-schema-evolution)
    - [Interoperability with `JSON_OBJ`](#interoperability-with-json_obj)
  - [Client-Side Enhancements](#client-side-enhancements)
    - [Task Workers](#task-workers)
      - [`StructDef` References](#structdef-references)
      - [Automatic `StructDef` Creation](#automatic-structdef-creation)
    - [Creating `VariableValue`s](#creating-variablevalues)
      - [Using an `@LHStruct` Annotated Class](#using-an-lhstruct-annotated-class)
      - [Using a Struct Name](#using-a-struct-name)
    - [The `WfSpec` DSL](#the-wfspec-dsl)
      - [Using and Creating `StructDef`s](#using-and-creating-structdefs)
      - [Accessing Sub-Structures](#accessing-sub-structures)
  - [`StructDef` Naming Conventions](#structdef-naming-conventions)
    - [RegEx](#regex)
      - [Why?](#why)
      - [Setting `StructDef` Names via SDKs](#setting-structdef-names-via-sdks)
  - [`StructDef` Field Naming Conventions](#structdef-field-naming-conventions)
    - [Example](#example)
    - [Convention](#convention)
  - [Further Discussion](#further-discussion)
    - [Deprecating JSON\_OBJ?](#deprecating-json_obj)
    - [External Events?](#external-events)
    - [Implementation and Performance](#implementation-and-performance)
    - [Testing](#testing)
    - [Further Work](#further-work)


Author: Colt McNealy

We want schemas on the LH Server for several reasons:

* Add type safety to WfSpec’s.
* Allow users of the upcoming _Output Topic_ feature to safely perform joins and transformations without worrying about type casting failures.
* Better developer experience.

We already have strong typing for primitive types (`INT`, `STR`, `DOUBLE`, `BOOL`). However, the `JSON_OBJ` and `JSON_ARR` fields are YOLO Wild West, which has caused problems and errors for our users.

This proposal proposes to introduce a new type of variable, a `Struct` (with an associated `StructDef` metadata object). The `Struct` is intended to replace the `JSON_ARR` and `JSON_OBJ` fields in the long term, and if we implement it properly (with sufficient support in the SDK-side), ideally we will have equally or even more convenient developer experience while introducing strong typing.

We will deprecate the `JSON_OBJ` and `JSON_ARR` variable types before the `1.0` release and we will remove them with `2.0` (which is not going to happen for at least 2-3 years).

## Server-Side Changes

At a high level, we will add a new type to the `VariableValue` and `TypeDefinition` messages. This type will be called `Struct` and `StructDef`, respectively. It will represent a schema for complex nested objects.

### `Struct` and `StructDef` Protobuf

```protobuf
// A Struct is a strongly-typed structure containing fields. The Struct is defined
// according to the `Schema` object.
message Struct {
  // The id of the schema.
  StructDefId struct_def_id = 1;
  // The content of the Struct
  InlineStruct struct = 2;
}

// An Array is a list structure containing a single type of data.
message Array {
  message StringArray {
    repeated string items = 1;
  }

  message DoubleArray {
    repeated double items = 1;
  }

  message BoolArray {
    repeated bool items = 1;
  }

  message IntArray {
    repeated int64 items = 1;
  }

  message BytesArray {
    repeated bytes items = 1;
  }

  message WfRunIdArray {
    repeated WfRunId items = 1;
  }

  message StructArray {
    repeated Struct items = 1;
  }

  message ArrayArray {
    repeated Array items = 1;
  }

  oneof value {
    StringArray json_obj_arr = 1;
    StringArray json_arr_arr = 2;
    DoubleArray double_arr = 3;
    BoolArray bool_arr = 4;
    IntArray int_arr = 5;
    BytesArray bytes_arr = 6;
    WfRunIdArray wf_run_id_arr = 7;
    StructArray struct_arr = 8;
    ArrayArray array_arr = 9;
  }
}

// An `InlineStruct` is a pre-validated set of fields that are part of a `Struct`.
message InlineStruct {
  // The fields in the inline struct.
  repeated StructField fields = 1;
}

// A StructField represents the value for a single field in a struct.
message StructField {
  // The value, which can be primitive or complex.
  oneof struct_value {
    // The `value` of the field is an untyped primitive `VariableValue`.
    VariableValue primitive = 1;
    // The `value` of the field is a complex `Struct`.
    InlineStruct struct = 2;
    // The `value` of the field is a list of fields.
    FieldList list = 3;
  }
  // A FieldList is a sub-structure of a `Struct`
  message FieldList {
    repeated StructField fields = 1;
  }
}

// A `SchemaFieldDef` defines a field inside a `StructDef`.
message StructFieldDef {
  // The type of the field.
  TypeDefinition field_type = 1;
  
  // The default value of the field, which should match the Field Type. If not
  // provided, then the field is treated as required.
  optional VariableValue default_value = 2;
}

// A `StructDef` is a versioned metadata object (tenant-scoped) inside LittleHorse
// that defines the structure and content of a variable value. It allows strong typing.
message StructDef {
  // The id of the `Schema`. This includes the version.
  StructDefId id = 1;

  // Optionally description of the schema.
  optional string description = 2;

  // When the `StructDef` was created.
  google.protobuf.Timestamp created_at = 3;

  // The `StructDef` defines the actual structure of any `Struct` using this `InlineStructDeff`.
  InlineStructDef struct_def = 4;
}

// An `InlineStructDef` is the actual representation of the Schema.
message InlineStructDef {
  // The fields in this schema.
  map<string, StructFieldDef> fields = 1;
}

// An InlineArrayDef is the actual representation of an Array's Schema.
message InlineArrayDef {
  TypeDefinition array_type = 1;
}

// Unique identifier for a `StructDef`.
message StructDefId {
  // The name of the `StructDef`.
  string name = 1;

  // The version of the `StructDef`.
  int32 version = 2;
}
```

#### Inline Structs

The `InlineStruct` and `InlineStructDef` serve the purpose of allowing lightweight usage of Structs for within a `WfSpec` (meaning that we don't need to create a `StructDef` metadata getable). This is nice to allow scoping, which is a feature missing from the Task / TaskDef protocol.

#### `StructDef` Versioning

The `StructDefId` will have a `version` field. We will (by default) have strict compatibility rules for versioning a `StructDef`.

_**NOTE:** There may be a reason to make the `StructDefId` have a `major_version` and `revision` field as well, just like the `WfSpecId`, to differentiate compatible from breaking evolutions. This could potentially be done in a follow-up Proposal._

### Existing Protobuf

[PR #1412](https://github.com/littlehorse-enterprises/littlehorse/pull/1412) allows for strong typing of `ExternalEvent`s in LittleHorse. It also laid the groundwork for supporting `StructDef`s by refactoring the following proto:

* Refactors:
  * `Variable`
  * `VariableType`
  * `TaskDefOutputSchema`
* New structures:
  * `ReturnType`
  * `TypeDefinition`

This Proposal will build upon the foundation that #1412 laid.

As of now, the `TypeDefinition` looks as follows:

```proto
message TypeDefinition {
  // The basic type of the value. Will become a `oneof` once StructDef's and Struct's
  // are implemented according to issue #880.
  VariableType type = 1;

  // For compatibility purposes.
  reserved 2, 3;

  // Set to true if values of this type contain sensitive information and must be masked.
  bool masked = 4;
}
```

We will extend it to look like the following:
```proto
message TypeDefinition {
  // One of the following will be set.
  oneof defined_type {
    // A primitive type
    VariableType primitive_type = 1;

    // Refers to a `StructDef`
    StructDefId struct = 5;

    // Inline-defined Struct definition (does not refer to a Global Getable)
    InlineStructDef inline_struct = 6;

    // Inline-defined Array definition
    InlineArrayDef inline_array = 7;
  }

  // For compatibility purposes.
  reserved 2, 3;

  // Set to true if values of this type contain sensitive information and must be masked.
  bool masked = 4;
}
```

The `VariableValue` proto will be extended as follows:

```proto
// VariableValue is a structure containing a value in LittleHorse. It can be
// used to pass input variables into a WfRun/ThreadRun/TaskRun/etc, as output
// from a TaskRun, as the value of a WfRun's Variable, etc.
message VariableValue {
  reserved 1;

  // The value held in this VariableValue. If this is unset, treat it as
  // a NULL.
  oneof value {
    // A String representing a serialized json object.
    string json_obj = 2;

    // ... Omitted ...

    // A Struct
    Struct struct = 9;

    // An Array
    Array array = 10;
  }
}
```

The only addition is the `Struct struct = 9;` field.

Note that the `Variable`, `VariableDef`, `ThreadVarDef`, `ReturnType`, `TaskDef`, `ExternalEventDef`, `WorkflowEventDef`, `UserTaskDef`, and other protobuf structures will **not** need to change. All of the changes will be encapsulated within the `TypeDefinition` and `VariableValue` messages.

### `StructDef` Schema Evolution

At first, we will allow only Fully-Compatible schema changes:
* Add optional fields
* Add required fields with a default
* Remove optional fields.

Making a `PutStructDefRequest` with an identical `InlineStructDef` to what already exists will not cause a new Schema to be created. This follows the idempotence pattern already observed with all of our metadata Getables (including `WfSpec`, `UserTaskDef`, `TaskDef`, etc).

```proto
// Compatibility types for StructDef evolution
enum StructDefCompatibilityType {
  // No updates are allowed.
  NO_SCHEMA_UPDATES = 0;

  // Allowed to make fully compatible (both backward-and-forward compatible)
  // changes to the `struct_def` in this request.
  FULLY_COMPATIBLE = 1;
}

// Request to create a new `StructDef`.
message PutStructDefRequest {

  // The name of the `StructDef`.
  string name = 1;

  // The description of the `StructDef`.
  optional string description = 2;

  // The actual schema for the `StructDef`.
  InlineStructDef struct_def = 3;

  // If both of the following are true: <br/>
  // - A `StructDef` with the specified `name` already exists, AND <br/>
  // - The `InlineStructDef` is different <br/>
  //
  // Then the request will be accepted or rejected based on the value of the
  // allowed_updates type.
  StructDefCompatibilityType allowed_updates = 4;
}

service LittleHorse {
  // ...

  // Creates a `StructDef`. Note that this request is idempotent: if you
  // make a request to create a `StructDef` identical to the currently-created
  // one with the same `name`, no new `StructDef` will be created. This is the
  // same behavior as `rpc PutWfSpec` and `rpc PutUserTaskDef`.
  //
  // For schema evolution / compatibility rules, see the `AllowedStructDefUpdate`
  // message within the `PutStructDefRequest`.
  rpc PutStructDef(PutStructDefRequest) returns(StructDef) {}

  // Deletes a specified `StructDef`.
  rpc DeleteStructDef(StructDefId) returns(google.protobuf.Empty) {}

  // Gets a specified `StructDef`.
  rpc GetStructDef(StructDefId) returns(StructDef) {}

  // ...
}
```

I (Colt) _do not like_ the idea of relaxing the allowed update types to introduce minorly-breaking changes in the `StructDef`. The reasons for this are beyond the scope of this proposal. However, I have designed the `PutStructDefRequest` to allow extending it to relax those compatibility restrictions if needed in the future.

#### Validating `StructDef` Schema Evolution

In addition to allowing users to set a compatibility type to be validated when putting a `StructDef`, we will also provide `StructDef` validation through a separate RPC: `RPC ValidateStructDef`.

`RPC ValidateStructDef` will perform the same compatibility checks used in `RPC PutStructDef`, but without the intent of putting a `StructDef` to the server. This make the server a useful single source of truth for checking compatibility when designing `StructDef`s.

```proto
// Request to compare a new `StructDef` against an existing `StructDef` based on a compatibility type.
message ValidateStructDefRequest {
  // The name of the `StructDef` you want to compare against that already exists on the server.
  string name = 1;

  // The new `StructDef` schema.
  InlineStructDef struct_def = 2;


  // The server will validate the new StructDef schema against
  // the existing StructDef schema based on this compatibility type.
  StructDefCompatibilityType compatibility_type = 3;
}

// Response containing information about whether or not a new `StructDef` is compatible with an existing `StructDef`.
message ValidateStructDefResponse {
  bool is_compatible = 0;

  // This message leaves room for returning additional information about the validation,
  // like what `StructFieldDef` evolution(s) are invaild and why.
}

service LittleHorse {
  // ...
  
  // Validate the schema of a new `StructDef` against an existing `StructDef`
  rpc ValidateStructDef(ValidateStructDefRequest) returns (ValidateStructDefResponse) {}

  // ...
}
```

### Interoperability with `JSON_OBJ`

Our conversion policy will be that we can convert a `Struct` to a `JSON_OBJ`, but we do not allow converting a `JSON_OBJ` to a `Struct`. For example:

* If a `WfSpec` passes a `Struct` into a `TaskDef` that expects a `JSON_OBJ`, the Server will automatically convert the `ScheduledTask`'s appropriate `VariableValue` to a `JSON_OBJ`.
* We will reject attempts to create a `WfSpec` that passes in a `JSON_OBJ` value into a `TaskDef` that expects a `Struct`.

Consider the following Java:

```java
class Car {
  String make;
  String model;
}

// the worker class
public class MyWorker {
    @LHTaskMethod("something-with-car")
    public void foo(Car car) {
        System.out.println(car.make);
    }
}
```

In the past, this only worked if the variable on the ScheduledTask was of the type `JSON_OBJ`. This ADR proposes that the variable should work if either of the following are true:
1. The variable in the ScheduledTask is type JSON_OBJ and can be deserialized into a Car, OR
2. The variable in the ScheduledTask is a Struct that matches.

This means that the SDK will dynamically convert the `VariableValue` into a `Car` whether it is a `STRUCT` or a `JSON_OBJ`.

## Client-Side Enhancements

We want to make it just as _fast_ to work with `Struct`s as it is to work with `JSON_OBJ` variables. This means that we need to support automatic `StructDef` / `InlineStructDef` creation within our SDK's to make it so that users do not need to manually build the `StructDef` protobuf.

Additionally, users complain about the difficulty of having to register metadata (eg. `ExternalEventDef`, `UserTaskDef`, `WorkflowEventDef`) before registering the `WfSpec`. We want to make it

This proposal shows how the behavior will work with the `sdk-java`. I believe the other three SDK's can be addressed in one (or three) follow-up proposals.

### Task Workers

Task Workers will be able to deal with `StructDef`s in two ways:

1. Refer to the `StructDefId` of a `Struct` that already exists.
2. Create their own `StructDef` using reflection.

#### `StructDef` References

We will extend the `LHType` annotation to allow specifying a `StructDefId`. Additionally Consider the following Java:

```java
class Car {
    String make;
    String model;
}

class MyWorker {
    @LHTaskMethod("something-with-car-struct")
    public void doSomethingWithStruct(@LHType(structDefName = "car") Struct car) {
        // car is the LH proto `Struct`
    }

    @LHTaskMethod("something-with-car-object")
    public void doSomethingWithCar(@LHType(structDefName = "car") Car car) {
        // car is deserialized into a `Car` POJO
    }
}
```

In the above code, the `LHTaskWorker#registerTaskDef()` and `LHTaskWorker#start()` methods would throw an error if:

1. The `car` `StructDef` does not exist.
2. The `car` `StructDef` is incompatible with the `Car` object (only in the second case above).

#### Automatic `StructDef` Creation

The `LHTaskWorker` should be extended to allow automatically creating a `StructDef` as per the following code:

```java
@LHStructDef(name = "car")
class Car {
    @LHStructField(required = true)
    String make;

    @LHStructField(required = false)
    String model;
}

class MyWorker {
    @LHTaskMethod("something-with-car")
    public void doSomethingWithStruct(Car car) {
        // The `registerTaskDef()` method will create a `StructDef` first.
    }
}
```

### Creating `VariableValue`s

It should be easy to create a `VariableValue` of type `STRUCT` using our SDK's. We will provide two mechanisms. Note again that this is the Java proposal; other SDK's will follow shortly after.

#### Using an `@LHStruct` Annotated Class

Let's say I have the following Java class which is used to create a `StructDef`:

```java
@LHStructDef(name = "car")
class Car {
    @LHStructField(required = true)
    String make;

    @LHStructField(required = false)
    String model;
}
```

I should be able to do the following:

```java
Car carPojo = ...;
LHconfig config = ...;

VariableValue carVariableValue = LHLibUtil.objToStruct(carPojo, config);
```

The `LHLibUtil#objToStruct` method should fetch the `StructDef` from the Server and validate that the current POJO is compatible with it. If it is not commpatible, an `Exception` will be thrown.

_(NOTE: We can extend the `LHConfig` to cache `StructDef`s to improve performance, but that may not be popular among the LH Grumpy Maintainers.)_

#### Using a Struct Name

We should also support the following. Note that there is no `LHStruct` annotation on the `Car`.

```java
class Car {
    String make;
    String model;
}
```

Create a `Variablevalue`:
```java
Car carPojo = ...;
LHconfig config = ...;

VariableValue carVariableValue = LHLibUtil.objToStruct(carPojo, config, "car");
```

### The `WfSpec` DSL

We should make it easy to work with `Struct`s and `StructDef`s in the `WfSpec` SDK.


#### Using and Creating `StructDef`s

Regarding the creation of `StructDef`s inside the `Workflow` utility class:
* It will be common for a `WfSpec` to accept `Struct`s as input variables.
* If a `WfSpec` is passing a `Struct` into a `TaskRun`, then it is fair to assume that the `StructDef` already exists, because otherwise the `TaskDef` couldn't exist.

#### Accessing Sub-Structures

Note that `Struct`s are nested object structures. You can have a field inside a `Struct` whose type is a completely separate `StructDef`. For example:

```java
@LHStructDef(name = "car")
class Car {
    @LHStructField(required = true)
    public String make;

    @LHStructField(required = false)
    public String model;
}

@LHStructDef(name = "person")
class Person {
    @LHStructField(required = true)
    public String name;

    @LHStructField(required = true)
    public List<Car> cars;
}
```

This is useful, because we can have a `TaskDef` that takes in a `car` Struct. Let's see how this might work:

```java
public void wfLogic(WorkflowThread wf) {

    WfRunVariable myPerson = wf.declareStructVar("my-person", Person.class);
    WfRunVariable carValue = wf.declareDouble("car-value");

    carValue.assign(wf.execute("calculate-price-of-car", myPerson.get("car")));
}
```

Note that the `.get()` should be recursive: it should allow fetching sub-fields.

Additionally, the `Workflow#registerWfSpec()` method should create the `person` `StructDef` using reflection from the `Person` java class.

## `StructDef` Naming Conventions

The name of a given `StructDef` will adhere to the LittleHorse Server Hostname standard, which says that:

- All letters are lower case
- The first character must be a letter or a number
- The last character must be a letter or a number
- In between characters can match any lower case letter, number, or hyphen

### RegEx

This standard is matched by the following Regular Expression:

```regex
[a-z0-9]([-a-z0-9]*[a-z0-9])?
```

The regular expression can be broken down into parts:

- `[a-z0-9]`
  - The first character must be within ranges `a-z` or `0-9`
- `([-a-z0-9]*[a-z0-9])?`
  - `(...)?` matches the expression in the parentheses zero to one times
  - `([-a-z0-9]...)`
    - Matches a character within ranges `a-z`, `0-9` or `-`
  - `(...*...)`
    - Matches the previous expression zero to unlimited times
  - `(...[a-z0-9])`
    - Matches a character within ranges `a-z`, `0-9`

#### Why?

This standard was set because `Getable` `ID`s, like a `StructDef` name, are stored within keys in the RocksDB state store embedded in the Server. These keys contain other special characters for delimitting data, such as `/`, `_`, and `__`.

Since `StructDef`s are a `Getable` object on the LittleHorse Server, we will adhere to this existing naming standard for `StructDef` names.

#### Setting `StructDef` Names via SDKs

The LittleHorse SDKs will allow users to set the name of a `StructDef` similarly to how users set the names of `TaskDef`s, using annotations and reflection where possible, and otherwise by passing the names as strings into a `register` method.

Here are some examples of how this will work:

**Java**

In Java, we will use a class annotation with a `String name` parameter.

```java
@LHStructDef(name = "car")
public class Car {
  ...
}
```

**Python**

In Python, we will use a class decorator with a `str name` parameter:

```python
@lh_struct_def(name="car")
class Car:
    ...
```

**Go**

In Go, we will pass in the name to whichever method registers the `StructDef`:

```go

const StructDefName string = "car"

type Car struct {
  ...
}

func main() {
  littlehorse.RegisterStructDef(config, Car{}, StructDefName)
}
```

> [!NOTE]  
> Go does include support for Field Tags, which when used effectively can emulate Field Annotations or Decorators available in other languages. Unfortunately, however, these Field Tags cannot be added to an entire struct. There are work arounds, like asking users to define a field `StructDefName string` inside of a struct, and then adding a tag to the type struct to give the entire struct a name. But in an effort to have no "special" or "reserved" field names, I don't think this is a good solution.

**.NET/C#**

In .NET/C#, we will use a class attribute with a `string Name` parameter:

```c#
[LHStructDef("car")]
public class Car
{
  ...
}
```

## `StructDef` Field Naming Conventions

`StructDef` Fields will adhere to a common standard that all SDKs can support or convert towards without breaking language conventions.

This is important, because one of my core principles when designing and proof-reading our SDKs is that any user should be able to port code from one SDK to another without fuss. 

### Example

For example, the following `StructDef` in Java and the following `StructDef` in Python should compile to the same `StructDef` protobuf message, converting the field names to common standard:

**Java**
```java
@LHStructDef(name="car")
public class Car {
  public String name;
  public int year = 1970; // sets default value
  public boolean isSold;
  public String vinNumberISO3779; // Ok, this one is extreme, but you gotta throw a curveball in there
}
```

**Python**
```python
@lh_struct_def(name="car")
class Car:
    name: str = None 
    year: int = 1970 // sets default value
    is_sold: bool = None
    vin_number_iso3779: str = None
```

**JSON Representation of Protobuf**
```json
{
  "id": {
    "name": "car",
  },
  "struct_def": {
    "name": {
      "field_type": {
        "type": "STR"
      }
    },
    "year": {
      "field_type": {
        "type": "INT"
      },
      "default_value": {
        "int": 1970
      }
    },
    "is_sold": {
      "field_type": {
        "type": "BOOL"
      }
    },
    "vin_number_iso3779": {
      "field_type": {
        "type": "STR"
      }
    }
  }
}
```

As shown above, if we use `snake_case` as the default convention at the protobuf message level, the Java SDK should be able to convert to `snake_case` for full compatibility with an identical `StructDef` class written in Python. Vice-versa, if we use `camelCase` as the default convention at the protobuf message level, the Python SDK should be able to convert to `camelCase` for full compatibility with an identical `StructDef` class written in Java.

Since we will be handling some of the conversion ✨magic✨ behind the scenes in order to support standard language naming conventions, the conversion should be straight forward and easy to understand.

### Convention

`StructDef` Field names will follow the lower `camelCase` convention, meaning:

- The field name must start with a lowercase letter
  - Why no numbers? Java, Python, Go, class attributes cannot start with numbers
- Subsequent characters must match the ranges `A-Z`, `A-Z`, and `0-9`
  - Why no underscores? We want Java, .NET, and Go users to design compatible fields without breaking their individual language naming conventions. Since underscores are typically only used in constants in Java, and private member variables in `C#`, this character breaks support for these languages. Sorry Python, you can keep your `snake_case`, we will just convert them to `camelCase` during the `StructDef` compilation process.
- For our case, field names will be `case insensitive`.
  - Why? This is because we'll be doing some magic on the Python side to convert lower `snake_case` to lower `camelCase`, and we don't want to users to worry about capitalization.

## Further Discussion

The information above should be sufficient to get a working implementation done in Java. However, I wanted to make some notes.

### Deprecating JSON_OBJ?

I do not like `JSON_OBJ` at all. However, we have real users using `JSON_OBJ` and as such cannot pull the rug out from under it (even though we are before 1.0). A proper deprecation and removal strategy is beyond the scope of this Proposal; however, once this Proposal is implemented we will be in a position to fully support both `Struct` and `JSON_OBJ` types (and they will be partially interoperable).

### External Events?

This proposal does not _directly_ discuss External Events. That is not an oversight: the `ExternalEventDef` contains a `TypeDefinition` so it will automatically inherit `Struct` capabilities.

### Implementation and Performance

Since this proposal is very API-driven rather than performance-driven, there aren’t many implementation details that are worthy of being noted in the proposal. However, some notes:

* We will need to make sure that any schema validation happening inside a `Command#process()` call is very fast.
* We want to avoid json deserialization as much as possible. This is part of the motivation for not using JSONSchema.

### Testing

We will write end to end tests.

### Further Work

Some further work includes:

* Differentiate between breaking and compatible evolutions in the `StructDefId` by following the `WfSpecId` pattern.
* **Important**: Follow-up proposal that shows how this will work with the other three SDK's (Python, Go, C#).
* Plugins and wrappers that allow converting well-known schema formats (eg. Protobuf, Avro, JsonSchema) into LittleHorse `Struct`s and `StructDef`s.
