namespace LittleHorse.Sdk.Utils
{
    public class FileManager
    {
        internal static Dictionary<string, string> GetDictFromPath(string filePath)
        {
            Dictionary<string, string> properties = File.ReadLines(filePath)
                .Select(line => line.Trim().Split('='))
                .Select(arr => (Key: arr[0].Trim(), Value: arr[1].Trim()))
                .GroupBy(x => x.Key)
                .ToDictionary(keyGroup => keyGroup.Key, keyGroup => keyGroup.First().Value);

            return properties;
        }
    }
}