using LittleHorse.Sdk.Worker;

namespace InterruptHandlerExample;

public class MyWorker
{
    [LHTaskMethod("some-task")]
    public async Task SomeTask() 
    {
        Console.WriteLine("Executing some-task");
        throw new Exception("My task has failed.");
    }
    
    [LHTaskMethod("my-task")]
    public async Task<string> MyTask() 
    {
        Console.WriteLine("Executing my-task");
        return "hello, there!";
    }
}