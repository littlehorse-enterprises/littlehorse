using LittleHorse.Sdk.Common.Proto;

namespace LittleHorse.Sdk.Workflow.Spec;

/// <summary>
/// Represents a collection of spawned threads that are fixed at the time of creation.
/// </summary>
public interface SpawnedThreads
{
    /// <summary>
    /// Builds a <see cref="WaitForThreadsNode"/> that represents the spawned threads.
    /// </summary>
    /// <returns>WaitForThreadsNode</returns>
    WaitForThreadsNode BuildNode();

    /// <summary>
    /// Creates a new instance of <see cref="SpawnedThreads"/> with the specified spawned threads.
    /// </summary>
    /// <param name="threads">Any number spawned threads.</param>
    /// <returns></returns>
    static SpawnedThreads Of(params SpawnedThread[] threads) 
    {
        return new FixedSpawnedThreads(threads);
    }
}