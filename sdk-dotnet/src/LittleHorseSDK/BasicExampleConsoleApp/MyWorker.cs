
using LittleHorse.Worker.Attributes;
using Microsoft.Extensions.Logging;

namespace BasicExampleConsoleApp
{
    public class MyWorker
    {
        private readonly ILogger? _logger;
        public MyWorker(ILogger? logger = null) {
            _logger = logger;
        }

        [LHTaskMethod("greet")]
        public string Greeting(string name)
        {
            _logger?.LogDebug("Executing task greet");

            return $"Hello, {name}";
        }
    }
}
