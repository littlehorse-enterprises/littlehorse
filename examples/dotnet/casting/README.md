Dotnet Casting Example

This example demonstrates how to build a Workflow spec that uses primitive casts.

Prerequisites

- .NET SDK 8.0
- The `LittleHorse.Sdk` project available at `sdk-dotnet/LittleHorse.Sdk`
- LittleHorse server running and TaskDefs registered by workers before registering the workflow spec

Build & Run

```bash
cd examples/dotnet/casting
dotnet build
# To run the example that prints the spec (does not register it):
dotnet run --project CastingExample.csproj
```

Notes

- The example creates a `Workflow` instance and demonstrates `CastTo(...)` and convenience helpers like `CastToInt()`.
- Start workers that register TaskDefs before calling `RegisterWfSpec` on the client to avoid "Refers to nonexistent TaskDef" errors.
