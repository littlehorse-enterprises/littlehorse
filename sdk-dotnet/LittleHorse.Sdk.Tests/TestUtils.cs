using System;
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

    public static string GetContentFromFilePath(string directory, string fileName)
    {
        var basePath = Path.GetFullPath(Path.Combine(AppContext.BaseDirectory, "../../.."));
        var content =  File.ReadAllText(Path.Combine(basePath, directory, fileName));
        
        return content;
    }
    
    public static void RemoveDirectory(string directory)
    {
        var basePath = Path.GetFullPath(Path.Combine(AppContext.BaseDirectory, "../../.."));
        var directoryPath = Path.Combine(basePath, directory);
        if (!string.IsNullOrEmpty(directory) && Directory.Exists(directoryPath))
        { 
           Directory.Delete(Path.Combine(basePath, directory), recursive: true);
        }
    }
    
    public static void RemoveFile(string fileName)
    {
        var basePath = Path.GetFullPath(Path.Combine(AppContext.BaseDirectory, "../../.."));
        var filePath = Path.Combine(basePath, fileName);
        if (File.Exists(filePath))
        { 
            File.Delete(filePath);
        }
    }
}