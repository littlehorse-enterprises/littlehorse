package io.littlehorse.sdk.wfsdk;

/**
 * Type for the output of a `THROW_EVENT` node.
 */
public interface ThrowEventNodeOutput {
    /**
     * Instructs the LH DSL to attempt to create the associated WorkflowEventDef with the
     * provided class as the type definition. Allowed classes are String, Integer, Double,
     * Boolean, Map, and List. If the provided class is `null`, then the WorkflowEventDef
     * will have a null payload.
     * @param payloadClass the class of the payload.
     */
    public void registeredAs(Class<?> payloadClass);
}
