package io.littlehorse.sdk.worker;

/**
 * Mutable context object passed to checkpointable functions for emitting log output.
 */
public class CheckpointContext {
    private String logOutput;

    /**
     * Creates an empty checkpoint context.
     */
    public CheckpointContext() {
        this.logOutput = "";
    }

    /**
     * Appends a value to the checkpoint log output.
     *
     * @param thing value to append; null values are appended as the literal string {@code null}
     */
    public void log(Object thing) {
        if (thing != null) {
            logOutput += thing.toString();
        } else {
            logOutput += "null";
        }
    }

    /**
     * Returns all accumulated checkpoint log output.
     *
     * @return concatenated checkpoint log output
     */
    public String getLogOutput() {
        return logOutput;
    }
}
