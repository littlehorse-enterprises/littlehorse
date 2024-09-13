# LittleHorse Contributing .NET SDK

You need to package the project to generate the nuget file `LittleHorse.{version}.nupkg`

```dotnet pack```

How to set a local nuget directory packages to call its dependencies, you should specify where your .nupkg is created

```dotnet nuget add source {{your_local_path}}/littlehorse/sdk-dotnet/LittleHorse.Sdk/bin/Debug --name LocalNugetPackages```

next: call the dependency client csproj file specifying the local version

```<PackageReference Include="LittleHorse" Version="0.5.1-alpha1" />```

To remove local registered nuget packages use hits:

```dotnet nuget remove source {{local_source_name}}```

To create the Tests Project use:

```dotnet new xunit -o LittleHorse.Sdk.Tests```
