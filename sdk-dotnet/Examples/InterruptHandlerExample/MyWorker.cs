using LittleHorse.Sdk.Worker;

namespace InterruptHandlerExample;

public class MyWorker
{
    [LHTaskMethod("some-task")]
    public void SomeTask() 
    {
        Console.WriteLine("Executing some-task");
        throw new Exception("My task has failed.");
    }
    
    [LHTaskMethod("my-task")]
    public string MyTask() 
    {
        Console.WriteLine("Executing my-task");
        return "hello, there!";
    }
}