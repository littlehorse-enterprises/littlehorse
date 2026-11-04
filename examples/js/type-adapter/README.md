# Type Adapter

This example demonstrates how to register and use an sdk-js `LHTypeAdapter`.

It defines a Type Adapter so that task functions can work with a `UUID` class
while LittleHorse persists and transports values as `STR`.

## What this example does

1. Defines a `UUIDTypeAdapter` that implements `LHTypeAdapter<UUID>`.
2. Adds the `UUIDTypeAdapter` to the `LHConfig` with `addTypeAdapter`.
3. Defines a workflow `example-type-adapter` with a `uuid` workflow variable
   of type `STR`.
4. Executes task `get-uuid` whose return value is a `UUID` instance.
5. Executes task `echo-uuid`, which receives the UUID and logs it.

The SDK uses the adapter to map `UUID -> STR` when sending task output.

### Difference from the Java SDK

Java applies adapters in both directions and when registering TaskDef
schemas, because it can dispatch on the method signature's parameter and
return types. sdk-js has no runtime type tokens, so adapters apply on the
serialization path only (task outputs, checkpoints, exception content):

- `get-uuid` returns a `UUID` instance and the adapter encodes it as `STR`.
- `echo-uuid` receives the transported `STR` and converts it back with
  `UUID.fromString` inside the task function.
- TaskDef schemas are declared explicitly with zod (`z.string()`), which is
  the normal sdk-js registration path.

Built with the `sdk-js` **wfsdk**: the `WfSpec` is defined in TypeScript and
registered from code, no checked-in JSON, no `lhctl deploy`.

## Prerequisites

A running LittleHorse server; see [`../../README.md`](../../README.md) for the
one-command setup. Examples read `~/.config/littlehorse.config` when it
exists, else they connect to `localhost:2023`.

## Run it

Start the workers. This registers the TaskDefs and the WfSpec, then keeps
polling:

```bash
npm install
npm start
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

No `lhctl`? `npm run trigger` starts one run and prints the result.
