using Microsoft.Extensions.Configuration;

namespace LittleHorse.Common.Configuration.Extension
{
    public static class LHWorkerConfigurationBuilderExtension
    {
        public static IConfigurationBuilder AddLHWorkerConfiguration(this IConfigurationBuilder builder)
        {
            return builder.AddEnvironmentVariables();
        }

        public static IConfigurationBuilder AddLHWorkerConfiguration(this IConfigurationBuilder builder, string configFilePath)
        {
            builder.AddIniFile(configFilePath);
            builder.AddEnvironmentVariables();
            return builder;
        }

        public static IConfigurationBuilder AddLHWorkerConfiguration(this IConfigurationBuilder builder, Dictionary<string, string?> properties)
        {
            return builder.AddInMemoryCollection(properties);
        }
    }
}
