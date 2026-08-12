# Java Records as `StructDef` Types

Authors: Jacob Snarr

This proposal documents the semantics introduced by the `feat/sdk-java/support-records` branch, which extends the Java SDK to allow Java `record` types to be used as `@LHStructDef`-annotated classes.

## Motivation

Before this change, the Java SDK's `@LHStructDef` support only worked with POJOs that expose getter/setter pairs and a public no-arg constructor. Modern Java applications use Record types as immutable data carriers because:

- Records are more concise (no boilerplate getters/setters).
- Records are immutable, which aligns well with the LH `Struct` model where fields are set once on construction.

We usually strive for LittleHorse to be as easy to import into users existing architectures as possible. Adding Records will give users more flexibility and make it easier for users that already depend on Records to transition to LittleHorse.

## Public API Changes

There are no changes to protobuf or the LH Server. This is a Java SDK-only change.

A Java Record annotated with `@LHStructDef` can now be used anywhere a POJO-style `@LHStructDef` class was accepted:

- As a task method parameter or return type.
- When calling `LHLibUtil.objToVarVal(...)` or `LHLibUtil.varValToObj(...)`.
- When calling `LHLibUtil.serializeToStruct(...)`.
- When building a `StructDef` via `LHStructDefType`.

### Minimal Example

```java
@LHStructDef("person")
public record Person(String name, String address) {}
```

This generates the same `InlineStructDef` as the equivalent POJO (annotated with lombok Getter/Setter annotations for simplicity):

```java
@Getter
@Setter
@LHStructDef("person")
public class Person {
    private String name;
    private String address;
}
```

## Annotation Placement

`@LHStructField` and `@LHStructIgnore` annotations can be placed in two ways on a record.

### On the Record Component (Preferred)

Place the annotation directly on the component declaration:

```java
@LHStructDef("customer")
public record Customer(
        @LHStructField(name = "fullName", isNullable = true) String name,
        @LHStructField(masked = true) String ssn) {}
```

### On an Overridden Accessor Method

Annotations on overridden accessor methods are also respected:

```java
@LHStructDef("customer")
public record Customer(String name, String ssn) {

    @Override
    @LHStructField(name = "fullName", isNullable = true)
    public String name() {
        return name;
    }

    @Override
    @LHStructField(masked = true)
    public String ssn() {
        return ssn;
    }
}
```

Both styles produce identical `InlineStructDef` output. Annotation lookup checks the record component first, then the accessor method, then the backing field (consistent with the existing POJO annotation resolution order).

## Serialization Semantics

Serialization (object → `VariableValue`) for Record defined StructDefs works identically to POJO defined StructDefs. For each record component, the SDK invokes the compiler-generated accessor method (e.g. `name()` for a `String name` component) to read the field value, then delegates to the existing type conversion pipeline.

Type adapters registered with `LHTypeAdapterRegistry` are applied to individual component values in the same way they are for POJO fields:

```java
@LHStructDef("adapter-record")
public record AdapterRecord(UUID id, String name) {}

// UUID is serialized as STR via the registered adapter
VariableValue val = LHLibUtil.objToVarVal(new AdapterRecord(uuid, "alice"), registry);
```

## Deserialization Semantics

Deserialization (`VariableValue` → object) cannot use the POJO approach (create empty instance, then call setters field-by-field) because records are immutable. Instead, the SDK:

1. Looks up the `LHStructProperty` for each record component by name.
2. Reads the corresponding `StructField` from the incoming `Struct` payload.
3. Deserializes each field value into the component's declared Java type.
4. Calls the **canonical constructor** (the compiler-generated all-args constructor) with the deserialized values in component declaration order.

All fields present in the record's component list **must** be present in the incoming `Struct` payload. If a field is absent, deserialization throws an `LHSerdeException`.

## Default Values

The SDK computes default `StructFieldDef` values by instantiating the class with a no-arg constructor and reading the resulting field values. For records, the no-arg constructor does not exist by default, so **no default values are emitted** unless the record explicitly declares one:

```java
@LHStructDef("greeter")
public record Greeter(String greeting) {
    // Custom no-arg constructor delegates to canonical constructor
    public Greeter() {
        this("hello");
    }
}
```

In this example, the `greeting` field's `StructFieldDef` will include `default_value: {str: "hello"}`.

If a record does not declare a no-arg constructor, the SDK logs a warning and omits the default value — this is the same behavior as a POJO whose no-arg constructor is not visible.

## Limitations and Open Questions

### Nested Records

Nested records work as long as both the outer and inner record classes carry `@LHStructDef`. This mirrors the existing behavior for nested POJOs.

### Records with `List` or `Map` Components

Record components typed as `T[]` (native LH array) and `Map<K, V>` (native LH map) are supported, the same as with Classes.

### No SDK Changes for Other Languages

This change is Java-only. Go, Python, .NET, and JS SDK users interacting with `Struct` values produced from Java records see no difference — the wire format is identical to that of an equivalent POJO.
