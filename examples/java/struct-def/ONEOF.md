# Two Java APIs, one protobuf representation

Run the client-only example from the repository root:

```sh
./gradlew :example-struct-def:runOneOfExample
```

[OneOfStructExample.java](src/main/java/io/littlehorse/examples/OneOfStructExample.java)
prints identical schema/value comparisons and reads the same value into both
Java models. No server is needed.

Declare alternatives directly on record components (or POJO fields/accessors):

```java
@LHStructDef("payment")
public record FlatPayment(
        String orderId,
        @LHStructOneOf(group = "method", required = true) String cardToken,
        @LHStructOneOf(group = "method", required = true) String bankAccountId) {}
```

Or define a helper type:

```java
@LHStructOneOf(required = true)
public record PaymentMethod(String cardToken, String bankAccountId) {}

@LHStructDef("payment")
public record NestedPayment(String orderId, PaymentMethod method) {}
```

Both serialize to the same logical shape:

```json
{"orderId": "order-123", "method": {"cardToken": "tok_abc"}}
```

The flat API groups properties into `method` when writing and expands them when
reading. Local access remains `payment.cardToken()`. The nested API uses
`payment.method().cardToken()`. The group name becomes a real wire field, so
workflow paths use `method.cardToken` with either API.

`@LHStructOneOf` on a helper generates an inline schema and needs no separate
registration. Combining it with `@LHStructDef` on the same type is rejected.
Named StructDefs contain choices so they can gain ordinary fields alongside them.
`InlineStructDef.oneof_constraints` holds the selection rule. Values carry only
the nested Struct and its selected field; they do not repeat the rule.

`required` defaults to false for either placement. Omit `group` on types;
it is required on properties. Every flat group's members must agree on
`required`. An optional selection still emits the container: `"method": {}`.
The equivalent nested value is `new PaymentMethod(null, null)`. A null nested
parent requires the existing `@LHStructField(isNullable = true)` annotation and
has a different meaning from an empty choice.

Alternatives use reference types with no defaults or nullable annotation.
`null` means unselected; `false`, `0`, and empty strings/collections count as
selections. The SDK rejects multiple selections, a missing required selection,
explicit null alternatives on the wire, and unknown alternative names.
Missing alternatives become Java null on read.

Groups need at least one included field. Names follow Struct field naming
rules and cannot collide with serialized properties. Renames, ignores, masking,
and type adapters follow the existing property rules. String typos can silently create different groups,
including single-field groups. Use constants or the helper-type API to avoid that ambiguity.
A whole-type `@LHStructOneOf` cannot also declare field groups on its own alternatives.

This prototype changes protobuf definitions and the Java SDK. Server validation
and the other SDKs are not implemented yet.
