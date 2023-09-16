package io.littlehorse.sdk.wfsdk;

/**
 * This is the output of `ThreadBuilder::spawnThread()`. It is used as input for
 * `ThreadBuilder::waitForThread()`.
 */
public interface SpawnedThread {

    WfRunVariable getThreadNumberVariable();
}
