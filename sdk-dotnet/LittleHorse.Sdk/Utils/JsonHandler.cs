using System.Runtime.CompilerServices;
using Newtonsoft.Json;

[assembly: InternalsVisibleTo("LittleHorse.Sdk.Tests")]

namespace LittleHorse.Sdk.Utils;

internal static class JsonHandler
{
    internal static string ObjectSerializeToJson(object o)
    {
        var jsonSettings = new JsonSerializerSettings();

        return JsonConvert.SerializeObject(o, jsonSettings);
    }
    
    internal static object? DeserializeFromJson(string json, Type type)
    {
        return JsonConvert.DeserializeObject(json, type);
    }
}