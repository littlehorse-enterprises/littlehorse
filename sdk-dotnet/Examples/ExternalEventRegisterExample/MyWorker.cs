using LittleHorse.Sdk.Worker;

namespace ExternalEventRegisterExample
{
    public class MyWorker
    {
        [LHTaskMethod("print-message-externa-event")]
        public String PrintMessage(String message)
        {
            Console.WriteLine($"Message: {message}");
            return message;
        }
    }
}
