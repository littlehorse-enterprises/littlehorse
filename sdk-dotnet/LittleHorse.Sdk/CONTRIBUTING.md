# LittleHorse Contributing .NET SDK

Create a folder where to place local nuget packages

```mkdir $HOME/nuget-packages```

You need to package the project to generate the nuget file `LittleHorse.Sdk.{{version}}.nupkg` in a common folder

```
dotnet build
dotnet pack --output $HOME/nuget-packages
```

To set a local nuget directory packages to call its dependencies, you should specify where your .nupkg is created

```
dotnet nuget add source $HOME/nuget-packages --name LocalNugetPackages
```

Call the local dependency in your client csproj

```<PackageReference Include="LittleHorse.Sdk" Version="0.5.4-alpha" />```

To remove local registered nuget packages use hits:

```dotnet nuget remove source LocalNugetPackages```

To create the Tests Project use:

```dotnet new xunit -o LittleHorse.Sdk.Tests```
