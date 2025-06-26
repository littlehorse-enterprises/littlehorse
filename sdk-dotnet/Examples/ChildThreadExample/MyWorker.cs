using LittleHorse.Sdk.Worker;

namespace ChildThreadExample;

public class MyWorker
{
    [LHTaskMethod("parent-task-1")]
    public async Task<int> ParentTask1(int input)
    {
        Console.WriteLine("Executing parent-task-1");
        return input * 2;
    }
    
    [LHTaskMethod("child-task")]
    public async Task<string> ChildTask(int input)
    {
        Console.WriteLine("Executing child-task");
        
        return $"hi there, input was: {input}";
    }
    
    [LHTaskMethod("parent-task-2")]
    public async Task<string> ParentTask2()
    {
        Console.WriteLine("Executing parent-task-2");
        
        return "hello, there!";
    }
}