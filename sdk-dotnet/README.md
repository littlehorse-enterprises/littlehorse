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

- Install [.NET Core 6/7/8](https://dotnet.microsoft.com/en-us/download)

```
brew install dotnet
```

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
dotnet run --project ./Examples/BasicExample
dotnet run --project ./Examples/ExceptionsHandlerExample
dotnet run --project ./Examples/MaskedFieldsExample
dotnet run --project ./Examples/WorkerContextExample
```
