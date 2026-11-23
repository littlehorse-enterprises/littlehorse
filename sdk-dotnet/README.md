# LittleHorse .NET SDK

[LittleHorse](https://littlehorse.io) is a high-performance microservice orchestration engine that allows developers to build scalable, maintainable, and observable applications. The LittleHorse Runtime has uses in fields such as:

- Business Process Management
- Event-Driven Systems
- Logistics Management Applications
- Financial Transaction Processing
- And More.

For documentation on how to use this library, please go to [the LittleHorse website](https://littlehorse.io/docs/server).

> **This does not include the Workflow SDK**.

## Developing

### Dependencies

LittleHorse 1.4.0 requires applications to target .NET 10 or later. The package ships one `net10.0` assembly and supports .NET 10 and 11. If your application targets .NET 6–9, retarget it before upgrading.

Install the [.NET 10 SDK](https://dotnet.microsoft.com/download/dotnet/10.0) to build the library and run the examples. To run the full test suite, also install the [.NET 11 SDK](https://dotnet.microsoft.com/download/dotnet/11.0).

- Plugins for [VS Code](https://code.visualstudio.com/): [C# Dev Kit](https://marketplace.visualstudio.com/items?itemName=ms-dotnettools.csdevkit)

### Build

```
cd sdk-dotnet
dotnet build ./LittleHorse.Sdk
```
### Build and Run tests

```
dotnet build ./LittleHorse.Sdk
dotnet test ./LittleHorse.Sdk.Tests
```

### Run Examples

```
dotnet run --project ../examples/dotnet/BasicExample
dotnet run --project ../examples/dotnet/ExceptionsHandlerExample
dotnet run --project ../examples/dotnet/MaskedFieldsExample
dotnet run --project ../examples/dotnet/WorkerContextExample
```
