package io.littlehorse.sdk.wfsdk;

import io.littlehorse.sdk.common.proto.WaitForThreadsNode;
import io.littlehorse.sdk.wfsdk.internal.SpawnedThreadsImpl;

public interface SpawnedThreads {

    WaitForThreadsNode buildNode();
    static SpawnedThreads of(SpawnedThread... threads){
        return new SpawnedThreadsImpl(threads);
    }

}
