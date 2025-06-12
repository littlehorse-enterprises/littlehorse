using LittleHorse.Sdk.Worker;

namespace ConditionalsExample;

public class MyWorker
{
    [LHTaskMethod("task-a")]
    public string TaskA() {
        Console.WriteLine("Executing task-a");
        return "hello there A";
    }

    [LHTaskMethod("task-b")]
    public string TaskB() {
        Console.WriteLine("Executing task-b");
        return "hello there B";
    }

    [LHTaskMethod("task-c")]
    public string TaskC() {
        Console.WriteLine("Executing task-c");
        return "hello there C";
    }

    [LHTaskMethod("task-d")]
    public string TaskD() {
        Console.WriteLine("Executing task-d");
        return "hello there D";
    }
}