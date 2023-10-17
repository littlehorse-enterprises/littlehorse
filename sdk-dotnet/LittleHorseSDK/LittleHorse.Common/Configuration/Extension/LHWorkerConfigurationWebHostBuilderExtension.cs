using Microsoft.AspNetCore.Hosting;
using Microsoft.Extensions.Configuration;

namespace Common.Configuration.Extension
{
    public static class LHWorkerConfigurationWebHostBuilderExtension
    {

        public static IWebHostBuilder ConfigureLHWorker(this IWebHostBuilder webHostBuilder)
        {
            webHostBuilder.ConfigureAppConfiguration((context, config) =>
            {
                config.AddEnvironmentVariables();
            });

            return webHostBuilder;
        }

        public static IWebHostBuilder ConfigureLHWorker(this IWebHostBuilder webHostBuilder, string configFilePath)
        {
            webHostBuilder.ConfigureAppConfiguration((context, config) =>
            {
                config.AddIniFile(configFilePath);
                config.AddEnvironmentVariables();
            });

            return webHostBuilder;
        }

        public static IWebHostBuilder ConfigureLHWorker(this IWebHostBuilder webHostBuilder, Dictionary<string, string?> properties)
        {
            webHostBuilder.ConfigureAppConfiguration((context, config) =>
            {
                config.AddInMemoryCollection(properties);
            });

            return webHostBuilder;
        }
    }
}
