## Running Inline StructDef Example

Inline StructDefs embed a schema directly in a workflow or task definition. They are useful for POJOs that should be represented as LittleHorse `Struct` values without registering a separate, named `StructDef`.

This example uses the `DeliveryAddress` POJO without a class-level `@LHStructDef`, so it becomes an inline StructDef. Its `@LHStructField` annotations add descriptions to the embedded fields without turning it into a separately registered StructDef. The `DeliveryAddress` schema is embedded directly in the registered `Order` StructDef (via the `deliveryAddress` field), in the workflow's `WfSpec`, and in the `normalize-order` and `format-shipping-label` `TaskDef`s.

### Start the workers

Run the example application to register the `StructDef`, `TaskDef`s, and `WfSpec`, then start the task workers:

```bash
./gradlew example-inline-struct-def:run --args='workers'
```

Keep this process running. Unlike the regular StructDef example, the `DeliveryAddress` schema is not registered as a standalone `StructDef`; it is embedded inline as a field of the `Order` StructDef and in the `WfSpec` and `TaskDef`s. Only the named `Order` StructDef is registered.

Omitting the argument also starts the workers. The supported modes are `workers` and `inline`.

### Run an inline workflow

With the workers running, execute this in another terminal:

```bash
./gradlew example-inline-struct-def:run --args='inline'
```

This mode uses `Workflow.inlineWorkflow(...)` to submit a one-off workflow without
registering or looking up a WfSpec. It passes a sample `Order` as a literal to
`normalize-order`, passes that task's output to `format-shipping-label`, and returns
the shipping label as the workflow output:

```text
ORD-1234: 123 Main Street, Springfield 12345
```

The `order` StructDef and both TaskDefs must already be registered by the workers
mode. Inline workflow execution and inline StructDefs are separate features: the
former avoids registering a WfSpec, while the latter embeds a struct's schema.

The command prints the WfRun ID immediately without waiting for completion. Use
`lhctl get wfRun <wf_run_id>` and `lhctl list nodeRun <wf_run_id>` to inspect it.

### Run the registered workflow

In another terminal, use `lhctl` to provide the order as a JSON-formatted Struct variable. The nested `deliveryAddress` object is provided inline:

*Unnecessary spacing added to the Struct strings to fit the use case of the example, which involves trimming those Strings via our `normalize-order` TaskDef.*

```bash
lhctl run example-inline-struct-def order '{"orderId":"ORD-1234","deliveryAddress":{"street":"123 Main Street   ","city":"        Springfield","postalCode":"      12345"}}'
```

The command prints the workflow run ID. Use it to inspect the result:

```bash
lhctl get wfRun <wf_run_id>
lhctl list nodeRun <wf_run_id>
```
