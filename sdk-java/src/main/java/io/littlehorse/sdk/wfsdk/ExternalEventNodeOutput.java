package io.littlehorse.sdk.wfsdk;

import io.littlehorse.sdk.common.proto.CorrelatedEventConfig;
import java.io.Serializable;

public interface ExternalEventNodeOutput extends NodeOutput {
    /**
     * Adds a timeout to an ExternalEventNode.
     *
     * @param timeoutSeconds the timeout length.
     * @return the ExternalEventNodeOutput.
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

    /**
     * Allows users to set a correlationId on the `ExternalEventNode`. This allows the NodeRun
     * to be completed by CorrelatedEvents not just ExternalEvents.
     * @param correlationId is the correlationId.
     * @return the ExternalEventNodeOutput.
     */
    public ExternalEventNodeOutput withCorrelationId(Serializable correlationId);

    /**
     * Allows setting a CorrelatedEventConfig on the ExternalEventDef registration.
     * @param config is the CorrelatedEventConfig to be put on the registered ExternalEventDef.
     * @return this ExternalEventNodeOutput
     */
    public ExternalEventNodeOutput withCorrelatedEventConfig(CorrelatedEventConfig config);
}
