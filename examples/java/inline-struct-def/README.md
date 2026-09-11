## Running Inline StructDef Example

Inline StructDefs embed a schema directly in a workflow or task definition. They are useful for
POJOs that should be represented as LittleHorse `Struct` values without registering a separate,
named `StructDef`.

This example uses the unannotated `DeliveryAddress` POJO as an inline StructDef. The `DeliveryAddress`
schema is embedded directly in the registered `Order` StructDef (via the `deliveryAddress` field), in
the workflow's `WfSpec`, and in the `normalize-order` and `format-shipping-label` `TaskDef`s.

### Start the workers

Run the example application to register the `StructDef`, `TaskDef`s, and `WfSpec`, then start the
task workers:

```bash
./gradlew example-inline-struct-def:run
```

Keep this process running. Unlike the regular StructDef example, the `DeliveryAddress` schema is not
registered as a standalone `StructDef`; it is embedded inline as a field of the `Order` StructDef and
in the `WfSpec` and `TaskDef`s. Only the named `Order` StructDef is registered.

### Run the workflow

In another terminal, use `lhctl` to provide the order as a JSON input variable. The nested
`deliveryAddress` object is provided inline:

```bash
lhctl run example-inline-struct-def order '{"orderId":"ORD-1234","deliveryAddress":{"street":"123 Main Street","city":"Springfield","postalCode":"12345"}}'
```

The command prints the workflow run ID. Use it to inspect the result:

```bash
lhctl get wfRun <wf_run_id>
lhctl list nodeRun <wf_run_id>
```
