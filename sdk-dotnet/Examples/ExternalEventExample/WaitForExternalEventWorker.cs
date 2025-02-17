using LittleHorse.Sdk.Worker;

namespace ExternalEventExample;

public class WaitForExternalEventWorker
{
    [LHTaskMethod("ask-for-name")]
    public string AskForName() 
    {
        Console.WriteLine("Executing ask-for-name");
        return "Hi what's your name?";
    }
    
    [LHTaskMethod("greet")]
    public string Greet(string name) 
    {
        Console.WriteLine("Executing greet");
        return "Hello there, " + name;
    }
}