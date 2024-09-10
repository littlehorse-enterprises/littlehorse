# LittleHorse Contributing .NET SDK

This file will show how to install different Dotnet versions and pick one of them up in Ubuntu (22.04)

1. [Install](https://learn.microsoft.com/en-us/dotnet/core/install/linux-ubuntu-install?pivots=os-linux-ubuntu-2204&tabs=dotnet8#ubuntu-2204) different Dotnet supported versions
 for your Linux SO

Examples:

```
sudo apt-get update &&   sudo apt-get install -y dotnet-sdk-6.0
sudo apt-get update &&   sudo apt-get install -y dotnet-sdk-7.0 
sudo apt-get update &&   sudo apt-get install -y dotnet-sdk-8.0 
```

2. List installed versions

```
dotnet --list-sdks
6.0.132 [/usr/lib/dotnet/sdk]
7.0.119 [/usr/lib/dotnet/sdk]
8.0.108 [/usr/lib/dotnet/sdk]
```

> **The default selected version in the SO is the latest one**.

3. Add a global.json file in your Dotnet Project and set the desired, writting the following lines:

```version
{
    "sdk": {
      "version": "6.0.132"
    }
}
```
