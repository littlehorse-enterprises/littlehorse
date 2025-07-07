using LittleHorse.Sdk.Worker;

namespace ThrowEventRegisterExample
{
    public class MyWorker
    {
        public const string PrintMessageTaskDefName = "print-message";

        [LHTaskMethod(PrintMessageTaskDefName)]
        public Task<string> Greeting(string name)
        {
            var message = $"Hello team, This is a Dotnet Worker";
            Console.WriteLine($"Executing task greet {name}: " + message);
            return Task.FromResult(message);
        }
    }
}