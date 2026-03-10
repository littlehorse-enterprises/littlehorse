package io.littlehorse.sdk.wfsdk;

import io.littlehorse.sdk.common.proto.WaitForThreadsNode;
import io.littlehorse.sdk.common.proto.WaitForThreadsStrategy;
import io.littlehorse.sdk.wfsdk.internal.FixedSpawnedThreads;

/**
 * Collection-like handle for a set of spawned threads.
 */
public interface SpawnedThreads {

    /**
     * Builds the wait node for this spawned thread collection.
     *
     * @param strategy waiting strategy to apply
     * @return wait node definition
     */
    WaitForThreadsNode buildNode(WaitForThreadsStrategy strategy);

    /**
     * Creates a SpawnedThreads instance from explicit thread handles.
     *
     * @param threads spawned thread handles
     * @return spawned thread collection
     */
    static SpawnedThreads of(SpawnedThread... threads) {
        return new FixedSpawnedThreads(threads);
    }
}
