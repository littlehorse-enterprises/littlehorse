using LittleHorse.Sdk.Common.Proto;

namespace LittleHorse.Sdk.Workflow.Spec;

public class FixedSpawnedThreads: SpawnedThreads
{
    private readonly ICollection<SpawnedThread> _spawnedThreads;

    public FixedSpawnedThreads(params SpawnedThread[] spawnedThreads) 
    {
        _spawnedThreads = spawnedThreads;
    }
    
    public WaitForThreadsNode BuildNode()
    {
        var waitNode = new WaitForThreadsNode();
        var threads = new List<WaitForThreadsNode.Types.ThreadToWaitFor>();
        foreach (var spawnedThread in _spawnedThreads)
        {
            var threadToWaitFor = spawnedThread.BuildThreadToWaitFor();
            threads.Add(threadToWaitFor);
        }
        
        waitNode.Threads = new WaitForThreadsNode.Types.ThreadsToWaitFor { Threads = { threads } };
        
        return waitNode;
    }
}