# Java `LHTaskWorker` Support Generic Structs

This proposal allows a Task Method to interact with raw `io.littlehorse.sdk.common.proto.InlineStruct`s rather than forcing it to work with an `@LHStructDef`-annotated Java class. We utilize annotations on the Task Method and method arguments themselves to tell LittleHorse which `StructDef` to expect.

We also use the "placeholders" infrastructure so that users can configure the name of the `StructDef`s rather than hard-coding them.

## Public Changes

There will be no changes to the protobuf; only Java SDK.

We will:

* Add a `structDefName` field to the `LHType` annotation.
* Allow returning `InlineStruct` on `LHTaskMethod`s that have the `structDefName` set in the `LHType`.
* Allow accepting `InlineStruct`s as parameters which have the `structDefName` set on the `LHType` implementation.
* Use the taskdef placeholders to allow users to dynamically configure the `StructDef` name rather than hard coding them.

### Returning `InlineStruct`s

We'll allow users to accept a return protobuf `Struct` which is to be compliant with an existing `StructDef`. For example:

```java
@LHTaskMethod("create-customer")
@LHType(structDefName = "customer")
public InlineStruct createCustomer(String name, String email) {
    return InlineStruct.newBuilder()
            .putFields("name", LHLibUtil.objToVarVal(name))
            .putFields("email", LHLibUtil.objToVarVal(email))
            .putFields("id", LHLibUtil.objToVarVal(UUID.randomUUID().toString()))
            .build();
}
```

The purpose of the `@LHType` annotation here is to allow the `LHTaskWorker#registerTaskDef()` to know the output type of the `TaskDef` to create. We return a `LHTaskError` of type `VAR_SUB_ERROR` if, at runtime, the `StructDefId` is not compatible with the result of what was returned.

### Accepting `InlineStruct`s

The API for accepting an `InlineStruct` is similar:

```java
@LHTaskMethod("email-customer")
public void emailCustomer(@LHType(structDefName = "customer") InlineStruct customer, String content) {
    // omitted
}
```

### Using `LHTaskWorker` Placeholders

Suppose I have the following method signature:

```java
@LHTaskMethod("call-${model}-lookup-${outputName}")
@LHType(structDefName = "${outputName}")
public InlineStruct createThing(@LHType(structDefName = "${inputStructName}") InlineStruct foo) {
    // omitted
}
```

I can use the following code:

```java 
LHConfig config = new LHConfig();

Map<String, String> placeholders = new HashMap<>();
placeholders.put("model", "gpt-5-4-codex");
placeholders.put("inputStructName", "lookup-car-request");
placeholders.put("outputName", "car");

LHTaskWorker worker = new LHTaskWorker(this, "${taskName}", config, placeholders);
```

Then it would:

* Create a `TaskDef` named `call-gpt-5-4-codex-lookup-car`
* Return a `car` Struct
* Take in a `lookup-car-request` Struct

## Testing

We'll have unit tests in `sdk-java` to verify the interpolation of placeholders and also the serialization / deserialization of the `InlineStruct`s. We'll add an end-to-end test in the server directory to verify that the interpolation works. It will require editing `test-utils` to support passing in dynamic arguments.

Aside from the end-to-end tests in the server, there will be _no other changes_ in the `server/` directory.
