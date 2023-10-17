using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.Hosting;

namespace Common.Configuration.Extension
{
    public static class LHWorkerConfigurationDefaultHostBuilderExtension
    {
        public static IHostBuilder ConfigureLHWorker(this IHostBuilder hostBuilder)
        {
            hostBuilder.ConfigureAppConfiguration((context, config) =>
            {
                config.SetBasePath(Directory.GetCurrentDirectory());
                config.AddJsonFile("appsettings.json", optional: true, reloadOnChange: true);
                config.AddEnvironmentVariables();
            });

            return hostBuilder;
        }

        public static IHostBuilder ConfigureLHWorker(this IHostBuilder hostBuilder, string configFilePath)
        {
            hostBuilder.ConfigureAppConfiguration((context, config) =>
            {
                config.AddJsonFile(configFilePath);
            });

            return hostBuilder;
        }

        public static IHostBuilder ConfigureLHWorker(this IHostBuilder hostBuilder, Dictionary<string, string?> properties)
        {
            hostBuilder.ConfigureAppConfiguration((context, config) =>
            {
                config.AddInMemoryCollection(properties);
            });

            return hostBuilder;
        }
    }
}
