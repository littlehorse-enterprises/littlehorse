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

All code in this directory is licensed under the [GNU Affero General Public License, Version 3](https://www.gnu.org/licenses/agpl-3.0.en.html) and is copyright of LittleHorse Enterprises LLC.

## Developing

### Dependencies

LittleHorse 1.4.0 requires applications to target .NET 10 or later. The package ships one `net10.0` assembly and supports .NET 10 and 11. If your application targets .NET 6–9, retarget it before upgrading.

Install the [.NET 10 SDK](https://dotnet.microsoft.com/download/dotnet/10.0) to build the library and run the examples. To run the full test suite, also install the [.NET 11 SDK](https://dotnet.microsoft.com/download/dotnet/11.0).

- Plugins for [VS Code](https://code.visualstudio.com/): [C# Dev Kit](https://marketplace.visualstudio.com/items?itemName=ms-dotnettools.csdevkit)

### Build

```
dotnet build ./LittleHorse.Sdk
```

### Self-signed TLS certificate

According to [the official page](https://learn.microsoft.com/en-us/aspnet/core/grpc/troubleshoot?view=aspnetcore-7.0#call-a-grpc-service-with-an-untrustedinvalid-certificate): **The .NET gRPC client requires the service to have a trusted certificate.**
