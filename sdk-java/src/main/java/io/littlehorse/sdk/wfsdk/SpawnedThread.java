package io.littlehorse.sdk.wfsdk;

/**
 * This is the output of `ThreadBuilder::spawnThread()`. It is used as input for
 * `ThreadBuilder::waitForThread()`.
 */
public interface SpawnedThread {

    /**
     * Returns the variable containing the spawned thread number.
     *
     * @return workflow variable for the spawned thread number
     */
    WfRunVariable getThreadNumberVariable();
}
