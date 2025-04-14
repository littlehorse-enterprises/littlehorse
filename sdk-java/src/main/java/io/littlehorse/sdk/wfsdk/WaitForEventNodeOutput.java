package io.littlehorse.sdk.wfsdk;

public interface WaitForEventNodeOutput extends NodeOutput {
    /**
     * Adds a timeout to an ExternalEventNode.
     *
     * @param timeoutSeconds the timeout length.
     * @return the TaskNodeOutput.
     */
    public WaitForEventNodeOutput timeout(int timeoutSeconds);
}
