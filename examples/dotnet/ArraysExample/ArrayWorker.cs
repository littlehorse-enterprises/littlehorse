using LittleHorse.Sdk.Worker;

namespace Examples.ArraysExample;

public class ArrayWorker
{
    [LHTaskMethod("produce-array")]
    [LHType(masked: false, isLHArray: true)]
    public Task<long[]> ProduceArray()
    {
        Console.WriteLine("Producing native LittleHorse Array of type Long");
        return Task.FromResult(new long[] { 1L, 2L, 3L });
    }

    [LHTaskMethod("consume-array")]
    public Task<string> ConsumeArray([LHType(masked: false, isLHArray: true)] long[] arr)
    {
        var arrText = "[" + string.Join(", ", arr) + "]";
        Console.WriteLine($"Consuming LHArray: {arrText}");
        return Task.FromResult("consumed:" + arrText);
    }
}
