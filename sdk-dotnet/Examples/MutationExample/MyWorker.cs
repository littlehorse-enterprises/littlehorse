using LittleHorse.Sdk.Worker;

namespace MutationExample;

public class MyWorker
{
    [LHTaskMethod("spider-bite")]
    public async Task<string> SpiderBite(string name) 
    {
        Console.WriteLine("Executing spider-bite");
        var names = new List<string> { "Miles", "Peter" };
        
        if (names.Contains(name)) 
        {
            Console.WriteLine($"{name} got bitten");
            
            return "Spider-man";
        }
        
        return $"The spider bite has no effect on {name}";
    }
}