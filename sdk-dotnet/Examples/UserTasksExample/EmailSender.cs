using LittleHorse.Sdk.Worker;

namespace UserTasksExample;

public class EmailSender
{
    [LHTaskMethod("send-email")]
    public async Task SendEmail(string address, string content, LHWorkerContext workerContext) 
    {
        if (workerContext.GetUserId() != null) 
        {
            Console.WriteLine($"Received variable by {workerContext.GetUserId()}");
        } 
        else if (workerContext.GetUserGroup() != null) 
        {
            Console.WriteLine($"Received variable by {workerContext.GetUserGroup()}");
        }

        Console.WriteLine($"Sending email to {address}");
        Console.WriteLine($"Content: {content}");
    }
}