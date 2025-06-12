using LittleHorse.Sdk.Worker;

namespace WorkerContextExample;

public class MyWorker
{
    [LHTaskMethod("task")]
    public void ProcessTask(long requestTime, LHWorkerContext context)
    {
        context.Log("ProcessPayment");
        Console.WriteLine($"Processing request time: {requestTime}");
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