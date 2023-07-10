package io.littlehorse.jlib.wfsdk;

/**
 * A ThreadFunc is the interface an object must implement in order to define a
 * `ThreadSpec` programmatically using the Java Library.
 */
public interface ThreadFunc {
    /**
     * This function defines the logic of a `ThreadSpec`.
     * @param thread is the ThreadBuilder that can be used to control the logic of
     * the `ThreadSpec`.
     */
    public void threadFunction(ThreadBuilder thread);
}
