package io.littlehorse.sdk.worker;

/**
 * A checkpointable function allows you to checkpoint expensive work that you do not want
 * to have to re-do during a retry of a TaskRun.
 *
 * @param <T> the result type produced by the function
 */
public interface CheckpointableFunction<T> {

    /**
     * Run the logic.
     *
     * @param context checkpoint context used to emit logs during the operation
     * @return the computed result
     */
    public T run(CheckpointContext context);
}
