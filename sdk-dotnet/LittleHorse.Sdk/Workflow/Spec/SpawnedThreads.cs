using LittleHorse.Sdk.Common.Proto;

namespace LittleHorse.Sdk.Workflow.Spec;

public interface SpawnedThreads
{
    WaitForThreadsNode BuildNode();

    static SpawnedThreads Of(params SpawnedThread[] threads) {
        return new FixedSpawnedThreads(threads);
    }

}