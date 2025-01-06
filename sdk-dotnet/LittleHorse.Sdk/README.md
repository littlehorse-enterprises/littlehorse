# LittleHorse .NET SDK

[LittleHorse](https://littlehorse.io) is a high-performance microservice orchestration engine that allows developers to build scalable, maintainable, and observable applications. The LittleHorse Runtime has uses in fields such as:

- Business Process Management
- Event-Driven Systems
- Logistics Management Applications
- Financial Transaction Processing
- And More.

For documentation on how to use this library, please go to [the LittleHorse website](https://littlehorse.io/docs/server).

> **This does not include the Workflow SDK**.

## License

All code in this repository is covered by the [Server Side Public License, Version 1](https://spdx.org/licenses/SSPL-1.0.html). All code is intellectual property of LittleHorse Enterprises LLC.

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

### Self-signed TLS certificate

According to [the official page](https://learn.microsoft.com/en-us/aspnet/core/grpc/troubleshoot?view=aspnetcore-7.0#call-a-grpc-service-with-an-untrustedinvalid-certificate): **The .NET gRPC client requires the service to have a trusted certificate.**

