## Running Inline StructDef Example

Inline StructDefs embed a schema directly in a workflow or task definition. They are useful for
POJOs that should be represented as LittleHorse `Struct` values without registering a separate,
named `StructDef`.

This example uses the unannotated `DeliveryAddress` POJO as an inline StructDef. The workflow passes an address through the `normalize-address` and `format-shipping-label` tasks.

### Start the workers

Run the example application to register the `TaskDef`s and `WfSpec`, then start the task workers:

```bash
./gradlew example-inline-struct-def:run
```

Keep this process running. Unlike the regular StructDef example, this example does not register a
standalone `StructDef`; the `DeliveryAddress` schema is embedded in the `WfSpec` and `TaskDef`s.

### Run the workflow

In another terminal, provide the street, city, and postal code as arguments:

```bash
./gradlew example-inline-struct-def:run --args='"123 Main Street" Springfield 12345'
```

The command prints the workflow run ID. Use it to inspect the result:

```bash
lhctl get wfRun <wf_run_id>
lhctl list nodeRun <wf_run_id>
```
