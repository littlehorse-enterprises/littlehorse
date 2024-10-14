using System.Runtime.CompilerServices;

[assembly: InternalsVisibleTo("LittleHorse.Sdk.Tests")]

namespace LittleHorse.Sdk.Utils { 
    internal static class IntegerConverter
    {
        internal static int FromString(string? value)
        {
            try
            {
                if (string.IsNullOrEmpty(value))
                {
                    return 0;
                }
                
                return int.Parse(value);
            }
            catch (Exception ex)
            {
                throw new FormatException($"Unable to convert '{value}' to an integer.", ex);
            }
        }
    }
}