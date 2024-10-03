namespace LittleHorse.Sdk.Utils
{
    public class FileManager
    {
        internal static Dictionary<string, string> GetDictFromPath(string filePath)
        {
            try
            {
                Dictionary<string, string> properties = new Dictionary<string, string>();
                foreach (var tuples in File.ReadLines(filePath)
                             .Select(line => line.Trim().Split('='))
                             .Select(arr => (Key: arr[0].Trim(), Value: arr[1].Trim()))
                             .GroupBy(x => x.Key))
                    properties.Add(tuples.Key, tuples.First().Value);

                return properties;
            }
            catch (Exception ex)
            {
                if (ex is FileNotFoundException)
                    throw new FileNotFoundException($"File {filePath} does not exist.", 
                        ex.Message);
                throw;
            }
        }
    }
}