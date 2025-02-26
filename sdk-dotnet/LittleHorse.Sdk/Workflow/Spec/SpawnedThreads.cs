using LittleHorse.Sdk.Common.Proto;

namespace LittleHorse.Sdk.Workflow.Spec;

public interface SpawnedThreads
{
    WaitForThreadsNode BuildNode();

    static SpawnedThreads Of(SpawnedThread threads) {
        return new FixedSpawnedThreads(threads);
    }

}