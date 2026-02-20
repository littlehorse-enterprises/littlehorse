package io.littlehorse.sdk.wfsdk;

import io.littlehorse.sdk.common.proto.WaitForThreadsNode;
import io.littlehorse.sdk.common.proto.WaitForThreadsStrategy;
import io.littlehorse.sdk.wfsdk.internal.FixedSpawnedThreads;

public interface SpawnedThreads {

    WaitForThreadsNode buildNode(WaitForThreadsStrategy strategy);

    static SpawnedThreads of(SpawnedThread... threads) {
        return new FixedSpawnedThreads(threads);
    }
}
