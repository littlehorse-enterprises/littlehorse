using LittleHorse.Sdk.Worker;

namespace MaskedFieldsExample
{
    public class MyWorker
    {
        [LHTaskMethod("create-greet")]
        [LHType(masked: true)]
        public string CreateGreeting([LHType(masked: true)] string name)
        {
            var message = $"Hello team, This is a New Greeting for {name}";
            Console.WriteLine($"Executing task create greet {name}");
            return message;
        }
        
        [LHTaskMethod("update-greet")]
        public string UpdateGreeting([LHType(masked: true)] string name)
        {
            var message = $"Hello team, This is Greeting Modification {name}";
            Console.WriteLine($"Executing task update greet {name}");
            return message;
        }
        
        [LHTaskMethod("delete-greet")]
        public string DeleteGreeting(string name)
        {
            var message = $"Hello team, This is a Greeting Deletion {name}";
            Console.WriteLine($"Executing task delete greet {name}");
            return message;
        }
    }
}