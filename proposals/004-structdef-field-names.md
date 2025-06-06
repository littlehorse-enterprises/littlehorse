# Proposal: `StructDef` Naming Conventions

- [Proposal: `StructDef` Naming Conventions](#proposal-structdef-naming-conventions)
  - [Background](#background)
    - [Motivation](#motivation)
  - [Decision](#decision)
    - [`StructDef` Names](#structdef-names)
      - [RegEx](#regex)
      - [Why?](#why)
      - [Setting `StructDef` Names via SDKs](#setting-structdef-names-via-sdks)
    - [`StructDef` Field Names](#structdef-field-names)
      - [Vision](#vision)
      - [Concerns](#concerns)

Author: Jacob Snarr

## Background

This proposal will decide naming conventions for named parts of `StructDef`s, including the `StructDef` name and the `StructDef` field names.

### Motivation

- To establish a standard naming convention for `StructDef`s
- To decide how LittleHorse SDKs will handle this naming convention
- To create a naming standard that will be compatible with client SDKs that may automatically generate `StructDef` objects using reflection
- To decide how LittleHorse Server will handle this naming convention

- To make a naming convention that all supported languages can adhere to. Users of the LittleHorse SDKs should be able to port their StructDefs to any language and still maintain the exact same functionality and names/field names should match up 1:1.

## Decision

### `StructDef` Names

The name of a given `StructDef` will adhere to the LittleHorse Server Hostname standard, which says that:

- All letters are lower case
- The first character must be a letter or a number
- The last character must be a letter or a number
- In between characters can match any lower case letter, number, or hyphen

#### RegEx

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

### `StructDef` Field Names

#### Vision

The name of all `StructDef` Fields will adhere to a common standard that all SDKs can support without breaking language conventions.

This is important, because one of my core principles when designing and proof-reading our SDKs is that any user should be able to port code from one SDK to another without fuss. 

For example, the following `StructDef` in Java should compile to the same `StructDef` protobuf message as the following `StructDef` in Python:

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
    "version": ...,
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

As shown above, if we use `snake_case` as the default convention at the protobuf message level, the Java SDK should be able to convert to `snake_case` for full compatibility with an identical `StructDef` class written in Python. 

#### Concerns

Since we will be handling some of the conversion behind the scenes in order to support standard language naming conventions, the conversion should be straight forward and easy to understand.

A Java user should be able to take the attribute `vin_number_iso3779` from a Python class and should not have trouble porting the attribute name to the compatible Java field `vinNumberIso3779`. Our SDKs should support both styles for compiling to a common standard .