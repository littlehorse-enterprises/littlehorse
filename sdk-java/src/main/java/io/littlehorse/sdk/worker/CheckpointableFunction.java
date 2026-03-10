package io.littlehorse.sdk.worker;

/**
 * A checkpointable function allows you to checkpoint expensive work that you do not want
 * to have to re-do during a retry of a TaskRun.
 *
 * @param <T> the type of the value returned by the function
 */
public interface CheckpointableFunction<T> {

    /**
     * Run the logic.
     *
     * @param context is context about the current Checkpoint. Can be used to emit logs during the operation
     * @return the computed result
     */
    public T run(CheckpointContext context);
}
