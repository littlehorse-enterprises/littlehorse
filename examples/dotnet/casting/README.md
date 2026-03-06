# Casting Example (.NET)

This example demonstrates both automatic and manual type casting in LittleHorse workflows using the .NET SDK.

## Overview

This example illustrates how LittleHorse performs type conversions between workflow variables.

### Automatic Casting (no explicit cast required)

LittleHorse automatically converts between common compatible types, for example:

- `INT` → `DOUBLE`
- `INT` → `STRING`
- `DOUBLE` → `STRING`
- `BOOL` → `STRING`

### Manual Casting (explicit cast required)

Some conversions require explicit calls such as `CastTo(...)`:

- `STRING` → `DOUBLE`
- `DOUBLE` → `INT`
- `STRING` → `BOOL`
- JSON-path result → `INT` (when the type is ambiguous)

## Prerequisites

- .NET SDK 8.0
- The `LittleHorse.Sdk` project available at `sdk-dotnet/LittleHorse.Sdk`
- LittleHorse server running and TaskDefs registered by workers before registering the workflow spec

## Build & Run

1. Build and start the worker (registers TaskDefs, registers the WfSpec, then starts polling):

```bash
cd examples/dotnet/casting
dotnet build
dotnet run
```

2. In another terminal, start a workflow run (use `lhctl`):

```bash
lhctl run casting-workflow
```

### Trigger the error handler

To make the workflow enter the error handler, run the workflow with an input that causes the server to fail when casting `hello` to a boolean. This will cause the node at thread 0 position 4 to fail and the handler thread to run; after the handler completes the workflow continues.

```bash
lhctl run casting-workflow string-bool Hello --wfRunId error-handler
```

To inspect the failed node run:

```bash
lhctl get nodeRun error-handler 0 4
```

## Notes

- The example creates a `Workflow` instance and demonstrates `CastTo(...)` and convenience helpers like `CastToInt()`.
- Start workers that register TaskDefs before calling `RegisterWfSpec` on the client to avoid "Refers to nonexistent TaskDef" errors.
