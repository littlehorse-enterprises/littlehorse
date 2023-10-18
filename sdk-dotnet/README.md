# LittleHorse .NET SDK

<a href="https://littlehorse.dev/"><img alt="littlehorse.dev" src="https://img.shields.io/badge/-LittleHorse.dev-7f7aff"></a>
<a href="https://github.com/littlehorse-enterprises/littlehorse"><img alt="github" src="https://img.shields.io/badge/-LittleHorse-gray?logo=github&logoColor=white"></a>

For documentation on how to use this library, please go to [the LittleHorse website](https://littlehorse.dev).

For examples go to the [examples](./Examples/) folder.

> :warning: **This does not include the Workflow SDK**.

## Dependencies

- Install [.NET Core 7](https://dotnet.microsoft.com/en-us/download)
- Plugins for [VS Code](https://code.visualstudio.com/): [C# Dev Kit](https://marketplace.visualstudio.com/items?itemName=ms-dotnettools.csdevkit)

## Run Tests

```
cd sdk-dotnet/LittleHorseSDK
dotnet test
```

## Run Example

```
cd sdk-dotnet/Examples/BasicExample
dotnet run Program.cs
```

## Self-signed TLS certificate

According to [the official page](https://learn.microsoft.com/en-us/aspnet/core/grpc/troubleshoot?view=aspnetcore-7.0#call-a-grpc-service-with-an-untrustedinvalid-certificate): **The .NET gRPC client requires the service to have a trusted certificate.**.

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
