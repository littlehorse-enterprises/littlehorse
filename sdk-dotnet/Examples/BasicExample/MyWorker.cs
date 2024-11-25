using LittleHorse.Sdk.Worker;

namespace Examples.BasicExample
{
    public class MyWorker
    {
        [LHTaskMethod("greet-dotnet")]
        public string Greeting(string name)
        {
            var message = $"Hello team, This is a Dotnet Worker";
            Console.WriteLine($"Executing task greet {name}: " + message);
            return message;
        }
    }
}
