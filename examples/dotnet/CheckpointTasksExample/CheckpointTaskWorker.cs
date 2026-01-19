using LittleHorse.Sdk.Worker;

namespace CheckpointTasksExample;

public class MyWorker
{
    [LHTaskMethod("greet")]
    public async Task<string> Greet(string name, LHWorkerContext context)
    {
        int attemptNumber = context.AttemptNumber;

        string? result = context.ExecuteAndCheckpoint<string>((LHCheckpointContext checkpointContext) => {
            checkpointContext.Log("this is a checkpoint log");

            return "hello " + name + " from first checkpoint";
        });

        if (attemptNumber == 0)
        {
            throw new Exception("Throwing a failure in the second checkpoint to show how the checkpoint works");
        }

        result += context.ExecuteAndCheckpoint<string>((LHCheckpointContext checkpointContext) =>
        {
            return " and the second checkpoint";
        });

        return result + " and after the second checkpoint";
    }
}