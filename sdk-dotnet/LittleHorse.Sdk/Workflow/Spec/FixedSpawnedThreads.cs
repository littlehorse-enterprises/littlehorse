using LittleHorse.Sdk.Common.Proto;

namespace LittleHorse.Sdk.Workflow.Spec;

/// <summary>
/// Represents a collection of spawned threads that are fixed at the time of creation.
/// </summary>
public class FixedSpawnedThreads: SpawnedThreads
{
    private readonly ICollection<SpawnedThread> _spawnedThreads;

    /// <summary>
    /// Initializes a new instance of the <see cref="FixedSpawnedThreads"/> class with the specified spawned threads.
    /// </summary>
    /// <param name="spawnedThreads">Any quantity of spawned threads.</param>
    public FixedSpawnedThreads(params SpawnedThread[] spawnedThreads) 
    {
        _spawnedThreads = spawnedThreads;
    }
    
    /// <summary>
    /// Builds a <see cref="WaitForThreadsNode"/> that represents the spawned threads.
    /// </summary>
    /// <returns>WaitForThreadsNode</returns>
    public WaitForThreadsNode BuildNode(WaitForThreadsStrategy strategy)
    {
        var waitNode = new WaitForThreadsNode();
        var threads = new List<WaitForThreadsNode.Types.ThreadToWaitFor>();
        foreach (var spawnedThread in _spawnedThreads)
        {
            var threadToWaitFor = spawnedThread.BuildThreadToWaitFor();
            threads.Add(threadToWaitFor);
        }
        
        waitNode.Threads = new WaitForThreadsNode.Types.ThreadsToWaitFor { 
            Threads = { threads },
        };

        waitNode.Strategy = strategy;
        
        return waitNode;
    }
}