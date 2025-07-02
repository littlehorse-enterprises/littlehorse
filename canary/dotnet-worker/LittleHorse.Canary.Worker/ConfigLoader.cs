using Microsoft.Extensions.Configuration;

namespace LittleHorse.Canary.Worker;

public abstract class ConfigLoader
{
    public static IConfiguration Load(string? filePath = null)
    {
        var configBuilder = new ConfigurationBuilder()
            .SetBasePath(AppContext.BaseDirectory)
            .AddJsonFile("appsettings.json", false, false);

        if (filePath != null)
        {
            configBuilder.AddJsonFile(filePath, false, false);
        }

        return configBuilder.AddEnvironmentVariables().Build();
    }
}
