using LittleHorse.Sdk.Common.Proto;
using LittleHorse.Sdk.Workflow.Spec;
using LittleHorse.Sdk.Worker;

namespace Examples.Casting;

public class MyWorker
{
    [LHTaskMethod("double-method")]
    public async Task<double> DoubleMethod(double input)
    {
        Console.WriteLine($"Executing double-method with input: {input}");
        return input; 
    }

    [LHTaskMethod("int-method")]
    public async Task<int> IntMethod(int input)
    {
        Console.WriteLine($"Executing int-method with input: {input}");
        return input;
    }

    [LHTaskMethod("bool-method")]
    public async Task<bool> BoolMethod(bool input)
    {
        Console.WriteLine($"Executing bool-method with input: {input}");
        return input;
    }

    [LHTaskMethod("string-method")]
    public async Task<string> StringMethod(string input)
    {
        Console.WriteLine($"Executing string-method with input: {input}");
        return input;
    }
}
