using LittleHorse.Sdk.Worker;

namespace ChildThreadsForeachExample;

public class MyWorker
{
    [LHTaskMethod("task-executor")]
    public string TaskExecutor(string taskInput) 
    {
        Console.WriteLine($"ok, executing task with {taskInput}");

        return $"Executed task with input: {taskInput}";
    }
}