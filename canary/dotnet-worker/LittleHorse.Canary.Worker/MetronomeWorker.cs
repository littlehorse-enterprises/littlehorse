using LittleHorse.Sdk;
using LittleHorse.Sdk.Worker;
using Microsoft.Extensions.Logging;

namespace LittleHorse.Canary.Worker;

public class MetronomeWorker
{
    private const string TaskName = "canary-worker-task";

    private readonly ILogger<MetronomeWorker> _logger;
    private readonly BeatProducer _producer;
    private readonly LHTaskWorker<MetronomeWorker> _worker;

    public MetronomeWorker(ILogger<MetronomeWorker> logger, BeatProducer producer, LHConfig lhConfig)
    {
        _logger = logger;
        _producer = producer;
        _worker = new LHTaskWorker<MetronomeWorker>(this, TaskName, lhConfig);
        ShutdownHook.Add(this);
    }

    public async Task Start()
    {
        await _worker.RegisterTaskDef();
        await _worker.Start();
    }

    public void Close()
    {
        _worker.Close();
        // TODO: wait for worker to completely and gracefully shutdown once the isRunning or isClosed method of the Task worker is implemented
    }

    [LHTaskMethod(TaskName)]
    public async Task ExecuteTask(long startTime, bool sampleIteration, LHWorkerContext context)
    {
        _logger.LogDebug("Executing task {} {}/{}", TaskName, context.IdempotencyKey, context.AttemptNumber);
        if (sampleIteration)
        {
            var id = $"{context.IdempotencyKey}/{context.AttemptNumber}";
            var latency = DateTime.Now - DateTime.UnixEpoch.AddMilliseconds(startTime);
            await _producer.Send(latency, id);
        }
    }
}
