using LittleHorse.Sdk.Worker;

namespace ExternalEventExample;

public class WaitForExternalEventWorker
{
    private int _counter = 0;

    [LHTaskMethod("greet")]
    public async Task<string> Greet(string name)
    {
        Console.WriteLine("Hello there, " + name);

        return "greeted-" + name.ToLower() + "-" + _counter++;
    }
    [LHTaskMethod("summary")]
    public async Task<string> ShowSummary(string name, int age)
    {
        name = name.ToUpper();
        var summary = $"Summary: {name} is {age} years old";
        if (age < 18)
        {
            summary += " and is a minor.";
        }
        else
        {
            summary += " and an adult.";
        }

        return summary;
    }
}