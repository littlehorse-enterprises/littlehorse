# Java `LHTaskWorker` Support Generic Structs

This proposal allows a Task Method to 

## SDK Usage Example

### Simple Struct

In some cases we may want to interact with a `Struct` but we may not have a Java class that generated the `Struct`. This can be done as follows:


```java
@LHTaskMethod("upsert-customer" returnStructDef = )
public Customer upsert(Customer input) {
    return input;
}
```

Resulting Java SDK metadata:

- TaskDef name: `upsert-customer-acme`
- Input StructDefId name: `customer-acme`
- Return StructDefId name: `customer-acme`
- Registered StructDef name: `customer-acme`

## Design

### 1. Introduce a shared name resolver utility

Create an internal utility used by both task-name and struct-name resolution, rather than keeping task-name-only logic in `LHTaskWorker`.

Responsibilities:

- Resolve placeholders in a template string using a key/value map.
- Throw clear exception on missing keys.

### 2. Thread resolver context through Struct type construction

Update internal Java SDK plumbing so struct name generation can use resolved values:

- `TaskDefBuilder`
- `LHTaskSignature`
- `LHClassType.fromJavaClass(...)`
- `LHStructDefType`

Implementation shape (one option):

- Add overloads/constructors that accept placeholder values map.
- Keep current constructors for backward compatibility and default to no placeholder replacement.

### 3. Resolve `@LHStructDef` name at use sites

Wherever `LHStructDefType` currently uses `annotation.value()` directly (`TypeDefinition`, `StructDefId`, `toStructDef`, `toPutStructDefRequest`), use the resolved name.

### 4. Keep behavior deterministic

Resolution occurs once during task/struct schema materialization, not at task execution time.

