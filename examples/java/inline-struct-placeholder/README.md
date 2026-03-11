## Running InlineStruct Placeholder Example

This example shows how to:

- Return a raw `InlineStruct` from a task method and bind it to a `StructDef` via `@LHType(structDefName = ...)`.
- Accept a raw `InlineStruct` input with `@LHType(structDefName = ...)`.
- Resolve `@LHTaskMethod` and `@LHType(structDefName = ...)` placeholders from a single map.

**_NOTE_: This example is for advanced use-cases only, in which you don't know the exact struct you're using when writing the code. In almost all cases, it's simpler and better to use `@LHStructDef` annotation as shown in the [normal Struct example](../struct-def/README.md)**

### Start workers and register metadata

Run this first in one terminal:

```bash
./gradlew example-inline-struct-placeholder:run
```

This command registers:

- StructDef: `customer-acme`
- TaskDefs: `acme-create-customer`, `acme-email-customer`
- WfSpec: `example-inline-struct-placeholder`

### Run the workflow

In another terminal:

```bash
./gradlew example-inline-struct-placeholder:run --args 'Leia leia@rebellion.example Welcome-to-LittleHorse'
```

You should see a workflow run id in stdout.

You can inspect it with:

```bash
lhctl get wfRun <wf_run_id>
lhctl list taskRun <wf_run_id>
```
