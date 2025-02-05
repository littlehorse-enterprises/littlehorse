using LittleHorse.Sdk.Worker;

namespace ConditionalsExample;

public class MyWorker
{
    [LHTaskMethod("task-a")]
    public String TaskA() {
        Console.WriteLine("Executing task-a");
        return "hello there A";
    }

    [LHTaskMethod("task-b")]
    public String TaskB() {
        Console.WriteLine("Executing task-b");
        return "hello there B";
    }

    [LHTaskMethod("task-c")]
    public String TaskC() {
        Console.WriteLine("Executing task-c");
        return "hello there C";
    }

    [LHTaskMethod("task-d")]
    public String TaskD() {
        Console.WriteLine("Executing task-d");
        return "hello there D";
    }
}