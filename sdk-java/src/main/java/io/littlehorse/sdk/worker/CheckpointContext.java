package io.littlehorse.sdk.worker;

/**
 * Mutable context object passed to CheckpointableFunctions containing Checkpoint context. Can be used for emitting log output.
 */
public class CheckpointContext {
    private String logOutput;

    /**
     * Creates an empty Checkpoint context.
     */
    public CheckpointContext() {
        this.logOutput = "";
    }

    /**
     * Appends a value to the Checkpoint log output.
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
     * Returns all accumulated Checkpoint log output.
     *
     * @return concatenated Checkpoint log output
     */
    public String getLogOutput() {
        return logOutput;
    }
}
