# Example: Type Adapter

This example demonstrates how to register and use a Java SDK `TypeAdapter`.

It uses a `LHStringAdapter<UUID>` so that task methods can work with `UUID` while LittleHorse persists/transports values as `STR`.

## What this example does

1. Registers a `LHStringAdapter<UUID>` in `LHConfig`.
2. Defines a workflow `example-type-adapter` with a `uuid` workflow variable of type `STR`.
3. Executes task `get-uuid` whose Java return type is `UUID`.
4. Executes task `echo-uuid` whose Java parameter type is `UUID`.

The SDK uses the adapter to:

- map `UUID -> STR` when sending task output,
- map `STR -> UUID` when providing task input,
- register TaskDef argument/return schema as `STR`.

## Run the example

Start the example worker process:

```bash
./gradlew example-type-adapter:run
```

In another terminal, run the workflow:

```bash
lhctl run example-type-adapter
```

## Verify behavior

Inspect task runs for the workflow and confirm:

- `get-uuid` returns a UUID-like string value,
- `echo-uuid` succeeds and logs `Received UUID via adapter: ...`.

Helpful commands:

```bash
lhctl get wfRun <wf_run_id>
lhctl list nodeRun <wf_run_id>
lhctl get taskRun <wf_run_id> <task_run_global_id>
```

## Source files

- `TypeAdapterExample.java`: workflow + adapter registration + worker startup
- `Worker.java`: UUID task methods annotated with `@LHTaskMethod`