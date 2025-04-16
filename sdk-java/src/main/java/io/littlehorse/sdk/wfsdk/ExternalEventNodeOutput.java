package io.littlehorse.sdk.wfsdk;

public interface ExternalEventNodeOutput extends NodeOutput {
    /**
     * Adds a timeout to an ExternalEventNode.
     *
     * @param timeoutSeconds the timeout length.
     * @return the TaskNodeOutput.
     */
    public ExternalEventNodeOutput timeout(int timeoutSeconds);
}
