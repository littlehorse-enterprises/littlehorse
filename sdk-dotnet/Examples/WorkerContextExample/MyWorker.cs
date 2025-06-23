using LittleHorse.Sdk.Worker;

namespace WorkerContextExample;

public class MyWorker
{
    [LHTaskMethod("task")]
    public async Task ProcessTask(long requestTime, LHWorkerContext context)
    {
        context.Log("ProcessPayment");
        Console.WriteLine($"Processing request time: {requestTime}");
        Console.WriteLine($"The Workflow Run Id is: {context.WfRunId}");
        Console.WriteLine($"The Node Run Id is: {context.NodeRunId}");
        Console.WriteLine($"The Task Run Id is: {context.TaskRunId}");
        Console.WriteLine($"The Idempotency Key is: {context.IdempotencyKey}");
        Console.WriteLine($"The Attempt Number is: {context.AttemptNumber}");
        Console.WriteLine($"The Scheduled Time is: {context.ScheduledTime}");
        Console.WriteLine($"The User Group is: {context.UserGroup}");
        Console.WriteLine($"The User Id is: {context.UserId}");
    }
}