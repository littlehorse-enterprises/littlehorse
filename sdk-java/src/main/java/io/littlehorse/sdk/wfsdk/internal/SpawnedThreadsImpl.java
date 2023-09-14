package io.littlehorse.sdk.wfsdk.internal;

import io.littlehorse.sdk.common.proto.WaitForThreadsNode;
import io.littlehorse.sdk.wfsdk.SpawnedThread;
import io.littlehorse.sdk.wfsdk.SpawnedThreads;
import lombok.Getter;

import java.util.Collection;
import java.util.List;

@Getter
public final class SpawnedThreadsImpl implements SpawnedThreads {

    private final Collection<SpawnedThread> spawnedThreads;

    public SpawnedThreadsImpl(SpawnedThread... spawnedThreads) {
        this.spawnedThreads = List.of(spawnedThreads);
    }

    @Override
    public WaitForThreadsNode buildNode() {
        return null;
    }
}
