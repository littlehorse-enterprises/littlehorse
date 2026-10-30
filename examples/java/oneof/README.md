# Oneof payment workflow

This example sends a payment through two tasks. One task reads it using annotations
on individual fields; the other uses a separate `PaymentMethod` type. Both use the
same registered `payment` StructDef.

The example requires a running LittleHorse server and uses
`~/.config/littlehorse.config` and the usual `LHC_*` environment variables.
Server support for oneofs is still pending in this prototype, so registration or
execution may fail until that support is implemented.

## Start the workers

From the repository root, run:

```sh
./gradlew :example-oneof:run
```

This registers the `payment` StructDef, both TaskDefs, and the `oneof-payment`
WfSpec, then starts the workers. Leave it running.

## Run the workflow

In another terminal, submit a payment using the field annotations:

```sh
./gradlew :example-oneof:run --args='fields'
```

Or submit the same payment using the helper type:

```sh
./gradlew :example-oneof:run --args='helper'
```

Each command prints the WfRun ID returned by the server. Both tasks should return
`Order order-123 uses a card`. Inspect the run and task results with:

```sh
lhctl get wfRun <wf_run_id>
lhctl list nodeRun <wf_run_id>
lhctl get taskRun <wf_run_id> <task_run_global_id>
```

See [OneOfExample.java](src/main/java/io/littlehorse/examples/OneOfExample.java)
for registration and submission, and
[OneOfPaymentWorker.java](src/main/java/io/littlehorse/examples/OneOfPaymentWorker.java)
for the two task methods.

## Declare the payment

In [FlatPayment.java](src/main/java/io/littlehorse/examples/FlatPayment.java),
declare alternatives directly on record components (or POJO fields/accessors):

```java
@LHStructDef("payment")
public record FlatPayment(
        String orderId,
        @LHStructOneOf(group = "method", required = true) String cardToken,
        @LHStructOneOf(group = "method", required = true) String bankAccountId,
        @LHStructOneOf(group = "method", required = true) String walletId) {}
```

Or use [NestedPayment.java](src/main/java/io/littlehorse/examples/NestedPayment.java)
with a helper defined in [PaymentMethod.java](src/main/java/io/littlehorse/examples/PaymentMethod.java):

```java
@LHStructOneOf(required = true)
public record PaymentMethod(String cardToken, String bankAccountId, String walletId) {}

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
`InlineStructDef.oneof_config` holds the selection rule. Values carry only
the nested Struct and its selected field; they do not repeat the rule.

`required` defaults to false for either placement. Omit `group` on types;
it is required on properties. Every flat group's members must agree on
`required`. An optional selection still emits the container: `"method": {}`.
The equivalent nested value is `new PaymentMethod(null, null, null)`. A null nested
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
