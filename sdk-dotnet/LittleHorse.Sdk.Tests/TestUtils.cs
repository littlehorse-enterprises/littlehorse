using System.Collections.Generic;
using System.IO;

namespace LittleHorse.Sdk.Tests;

public static class TestUtils
{
    public static void WriteContentInFile(Dictionary<string, string> content, string fileName)
    {
        using (StreamWriter writer = new StreamWriter(fileName))
        {
            foreach (var kvp in content)
            {
                writer.WriteLine($"{kvp.Key}={kvp.Value}");
            }
                
            writer.Flush();
            writer.Close();
        }
    }

    public static string BuildFilePath(string fileName)
    {
        string parentDirectory = Path.Combine(Directory.GetCurrentDirectory(), "Resources");
        string subDirectory = Path.Combine(parentDirectory, "tmp");
        Directory.CreateDirectory(subDirectory);
        string filePath = Path.Combine(subDirectory, fileName);

        return filePath;
    }
}