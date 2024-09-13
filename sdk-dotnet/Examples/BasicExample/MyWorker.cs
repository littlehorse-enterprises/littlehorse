
using LittleHorse.Worker.Attributes;
using Microsoft.Extensions.Logging;

namespace Examples.BasicExample
{
    public class MyWorker
    {
        private ILogger<MyWorker>? _logger;
        public MyWorker(ILogger<MyWorker>? logger = null)
        {
            _logger = logger;
        }

        [LHTaskMethod("greet-dotnet")]
        public string Greeting()
        {
            var message = $"Hello team, This is a Dotnet Worker";
            Console.WriteLine("Executing task greet: " + message);
            _logger?.LogInformation("Executing task greet: " + message);
            return message;
        }
    }
}
