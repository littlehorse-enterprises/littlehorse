using Microsoft.Extensions.Configuration;

namespace LittleHorse.Canary.Worker;

public static class Extensions
{
    public static Dictionary<string, string> ToDictionary(this IConfigurationSection  section)
    {
        var result = new Dictionary<string, string>();
        foreach (var config in section.GetChildren().Where(config => config.Value != null))
        {
            var key = config.Key.Replace("_", ".").ToLower();
            if (config.Value != null)
            {
                result[key] = config.Value;
            }
        }
        return result;
    }
}