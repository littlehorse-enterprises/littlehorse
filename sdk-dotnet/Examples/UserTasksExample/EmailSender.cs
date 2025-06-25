using LittleHorse.Sdk.Worker;

namespace UserTasksExample;

public class EmailSender
{
    [LHTaskMethod("send-email")]
    public async Task SendEmail(string address, string content, LHWorkerContext workerContext)
    {
        if (workerContext.UserId != null)
        {
            Console.WriteLine($"Received variable by {workerContext.UserId}");
        }
        else if (workerContext.UserGroup != null)
        {
            Console.WriteLine($"Received variable by {workerContext.UserGroup}");
        }

        Console.WriteLine($"Sending email to {address}");
        Console.WriteLine($"Content: {content}");
    }
}