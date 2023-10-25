
using LittleHorse.Worker.Attributes;
using Microsoft.Extensions.Logging;

namespace Examples.BasicExample
{
    public class MyWorker
    {
        private readonly ILogger? _logger;
        public MyWorker(ILogger? logger = null)
        {
            _logger = logger;
        }

        [LHTaskMethod("greet")]
        public string Greeting(string name)
        {
            var message = $"Hello, {name}";
            _logger?.LogInformation("Executing task greet: " + message);
            return message;
        }
    }
}
