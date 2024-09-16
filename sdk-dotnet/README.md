# LittleHorse .NET SDK

[LittleHorse](https://littlehorse.dev) is a high-performance microservice orchestration engine that allows developers to build scalable, maintainable, and observable applications. The LittleHorse Runtime has uses in fields such as:

- Business Process Management
- Event-Driven Systems
- Logistics Management Applications
- Financial Transaction Processing
- And More.

For documentation on how to use this library, please go to [the LittleHorse website](https://littlehorse.dev).

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

### Build and Run the SDK

```
cd sdk-dotnet/LittleHorse.Sdk
dotnet build
dotnet run
```

### Run Example

```
cd sdk-dotnet/Examples/BasicExample
dotnet run Program.cs
```

### Self-signed TLS certificate

According to [the official page](https://learn.microsoft.com/en-us/aspnet/core/grpc/troubleshoot?view=aspnetcore-7.0#call-a-grpc-service-with-an-untrustedinvalid-certificate): **The .NET gRPC client requires the service to have a trusted certificate.**.

> The configuration `LHC_CA_CERT` was remove for this implementation.

Every OS has a collection of CA certificates. On ubuntu:

```
./local-dev/issue-certificates.sh
sudo apt install -y ca-certificates
sudo cp local-dev/certs/ca/ca.crt /usr/local/share/ca-certificates
sudo update-ca-certificates
```

On windows:

1. Double click on the CA certificate file
2. Click on Install Certificate
3. Select "Local Machine" and click on Next
4. Select "Place all certificates in the following store" and click on Browse
5. Select "Trusted Root Certification Authorities" and click on OK, and then click on Next
6. Click on Finish
