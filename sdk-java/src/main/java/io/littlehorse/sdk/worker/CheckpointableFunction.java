package io.littlehorse.sdk.worker;

/**
 * A checkpointable function allows you to checkpoint expensive work that you do not want
 * to have to re-do during a retry of a TaskRun.
 */
public interface CheckpointableFunction<T> {

    /**
     * Run the logic.
     * @return returns the result.
     */
    public T run(CheckpointContext context);
}
