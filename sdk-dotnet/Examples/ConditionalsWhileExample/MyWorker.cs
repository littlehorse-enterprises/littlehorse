using LittleHorse.Sdk.Worker;

namespace ConditionalsWhileExample;

public class MyWorker
{
    [LHTaskMethod("eating-donut")]
    public string EatingDonut(int donutsLeft)
    {
        int left = donutsLeft - 1;
        string message = "eating donut, " + left + " left";
        Console.WriteLine(message);
        return message;
    }
}