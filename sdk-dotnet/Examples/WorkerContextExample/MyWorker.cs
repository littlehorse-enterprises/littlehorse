using LittleHorse.Sdk.Worker;

namespace WorkerContextExample;

public class MyWorker
{
    [LHTaskMethod("process-payment")]
    public void ProcessPayment(LHWorkerContext context)
    {
        context.Log("ProcessPayment");
        Console.WriteLine($"The Workflow Run Id is: {context.GetWfRunId()}");
        Console.WriteLine($"The Node Run Id is: {context.GetNodeRunId()}");
        Console.WriteLine($"The Task Run Id is: {context.GetTaskRunId()}");
        Console.WriteLine($"The Idempotency Key is: {context.GetIdempotencyKey()}");
        Console.WriteLine($"The Attempt Number is: {context.GetAttemptNumber()}");
        Console.WriteLine($"The Scheduled Time is: {context.GetScheduledTime()}");
        Console.WriteLine($"The User Group is: {context.GetUserGroup()}");
        Console.WriteLine($"The User Id is: {context.GetUserId()}");
    }
}