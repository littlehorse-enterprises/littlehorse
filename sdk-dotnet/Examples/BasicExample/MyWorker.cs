
using LittleHorse.Worker.Attributes;

namespace Examples.BasicExample
{
    public class MyWorker
    {
        [LHTaskMethod("greet-dotnet")]
        public string Greeting()
        {
            var message = $"Hello team, This is a Dotnet Worker";
            Console.WriteLine("Executing task greet: " + message);
            return message;
        }
    }
}
