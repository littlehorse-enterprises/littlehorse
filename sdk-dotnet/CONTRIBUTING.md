# Contributing to the LittleHorse .NET SDK

Install the .NET 10 and 11 SDKs for your platform using [Microsoft’s installation guide](https://learn.microsoft.com/dotnet/core/install/). Check the installed SDKs and runtimes:

```sh
dotnet --list-sdks
dotnet --list-runtimes
```

From the repository root, run the suite with the .NET 11 SDK selected. The test project runs once on each runtime:

```sh
dotnet test sdk-dotnet/LittleHorse.Sdk.Tests
```

Release builds use the .NET 10 SDK to pack the library. CI runs the same suite against that NuGet package on .NET 10 and 11.
