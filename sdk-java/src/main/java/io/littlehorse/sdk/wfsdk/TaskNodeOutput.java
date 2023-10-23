package io.littlehorse.sdk.wfsdk;

public interface TaskNodeOutput extends NodeOutput {

    /**
     * Configure number of retries on this specific Task Node.
     * @param retries is the number of times to retry failed executions of
     * TaskRuns on this Task Node.
     */
    public void withRetries(int retries);
}
