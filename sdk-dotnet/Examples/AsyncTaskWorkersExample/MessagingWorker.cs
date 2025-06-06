using LittleHorse.Sdk.Worker;

namespace AsyncTaskWorkersExample;

public class MessagingWorker
{
    [LHTaskMethod("send-email")]
    public async Task SendEmail(string receiver)
    {
        string result = await ProcessSendEmail();
        Console.WriteLine($"Hello, {receiver}!. {result}");
    }

    private static async Task<string> ProcessSendEmail()
    {
        // Simulate sending an email operation
        await Task.Delay(2000);
        
        return "Send email completed!";
    }
}
