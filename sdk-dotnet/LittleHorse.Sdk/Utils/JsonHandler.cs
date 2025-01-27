using System.Runtime.CompilerServices;
using LittleHorse.Sdk.Exceptions;
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
        try
        {
            return JsonConvert.DeserializeObject(json, type);
        }
        catch (Exception e)
        {
            throw new LHJsonProcessingException(e.Message);
        }
    }
}