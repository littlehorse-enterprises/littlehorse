using LittleHorse.Sdk.Worker;

namespace ThrowEventRegisterExample
{
    public class MyWorker
    {
        [LHTaskMethod("print-message")]
        public string Greeting(string name)
        {
            var message = $"Hello team, This is a Dotnet Worker";
            Console.WriteLine($"Executing task greet {name}: " + message);
            return message;
        }
    }
}
