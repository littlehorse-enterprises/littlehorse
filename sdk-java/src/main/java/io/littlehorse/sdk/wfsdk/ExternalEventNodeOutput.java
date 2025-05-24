package io.littlehorse.sdk.wfsdk;

public interface ExternalEventNodeOutput extends NodeOutput {
    /**
     * Adds a timeout to an ExternalEventNode.
     *
     * @param timeoutSeconds the timeout length.
     * @return the TaskNodeOutput.
     */
    public ExternalEventNodeOutput timeout(int timeoutSeconds);

    /**
     * Instructs the LH DSL to attempt to create the associated ExternalEventDef with the
     * provided class as the type definition. Allowed classes are String, Integer, Double,
     * and Boolean. If the provided class is `null`, then the ExternalEventDef will have
     * a null payload.
     * @param payloadClass the class of the payload.
     * @return the ExternalEventNodeOutput.
     */
    public ExternalEventNodeOutput registeredAs(Class<?> payloadClass);
}
