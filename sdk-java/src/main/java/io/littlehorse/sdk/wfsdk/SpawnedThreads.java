package io.littlehorse.sdk.wfsdk;

import io.littlehorse.sdk.common.proto.WaitForThreadsNode;
import io.littlehorse.sdk.wfsdk.internal.FixedSpawnThreads;

public interface SpawnedThreads {

    WaitForThreadsNode buildNode();

    static SpawnedThreads of(SpawnedThread... threads) {
        return new FixedSpawnThreads(threads);
    }
}
