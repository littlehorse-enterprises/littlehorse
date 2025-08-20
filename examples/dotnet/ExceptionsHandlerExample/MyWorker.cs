using LittleHorse.Sdk.Common.Proto;
using LittleHorse.Sdk.Worker;
using LHTaskException = LittleHorse.Sdk.Exceptions.LHTaskException;

namespace ExceptionsHandler
{
    public class MyWorker
    { 
        [LHTaskMethod("fail")]
        public async Task Fail()
        {
            Random random = new Random();
            int randomNumber = random.Next(6, 10);
            var message = $"Throw New Failing Task {randomNumber}.";
            if (randomNumber > 5)
            {
                throw new LHTaskException("Fail", message);
            }
            
            Console.WriteLine(message);
        }
        
        [LHTaskMethod("fail-new-process")]
        public async Task FailNewProcess()
        {
            Random random = new Random();
            int randomNumber = random.Next(1, 10);
            var message = $"Throw Other Failing Task {randomNumber}";
            if (randomNumber < 8)
            {
                VariableValue content = new VariableValue
                {
                    Str = "This is a problem"
                };
                throw new LHTaskException("Fail-New-Task", message, content);
            }
            
            Console.WriteLine(message);
        }
        
        [LHTaskMethod("technical-failure")]
        public async Task FailForTechnicalReason()
        {
            string message = null!;
            int result = message.Length;
            Console.WriteLine(result);
        }
        
        [LHTaskMethod("my-task")]
        public async Task<string> PassingTask()
        {
            Console.WriteLine("Executing passing task.");
            return "woohoo!";
        }
    }
}