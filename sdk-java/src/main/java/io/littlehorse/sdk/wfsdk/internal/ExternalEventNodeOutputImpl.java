package io.littlehorse.sdk.wfsdk.internal;

import io.littlehorse.sdk.common.proto.CorrelatedEventConfig;
import io.littlehorse.sdk.common.proto.PutExternalEventDefRequest;
import io.littlehorse.sdk.wfsdk.ExternalEventDefRegistration;
import io.littlehorse.sdk.wfsdk.ExternalEventNodeOutput;
import java.io.Serializable;

public class ExternalEventNodeOutputImpl extends NodeOutputImpl
        implements ExternalEventNodeOutput, ExternalEventDefRegistration {

    private final String externalEventDefName;
    private CorrelatedEventConfig correlatedEventConfig;
    private Class<?> payloadClass;

    public ExternalEventNodeOutputImpl(String nodeName, String externalEventDefName, WorkflowThreadImpl parent) {
        super(nodeName, parent);
        this.externalEventDefName = externalEventDefName;
    }

    @Override
    public ExternalEventNodeOutput timeout(int timeoutSeconds) {
        parent.addTimeoutToExtEvtNode(this, timeoutSeconds);
        return this;
    }

    @Override
    public ExternalEventNodeOutput registeredAs(Class<?> payloadClass) {
        this.payloadClass = payloadClass;
        parent.registerExternalEventDef(this);
        return this;
    }

    @Override
    public ExternalEventNodeOutput withCorrelationId(Serializable correlationId) {
        boolean shouldMaskAutomatically = (correlationId instanceof WfRunVariableImpl)
                && ((WfRunVariableImpl) correlationId).getTypeDef().getMasked();
        parent.addCorrelationIdToExtEvtNode(this, correlationId, shouldMaskAutomatically);
        if (correlatedEventConfig == null) correlatedEventConfig = CorrelatedEventConfig.getDefaultInstance();
        return this;
    }

    @Override
    public ExternalEventNodeOutput withCorrelationId(Serializable correlationId, boolean masked) {
        parent.addCorrelationIdToExtEvtNode(this, correlationId, masked);
        if (correlatedEventConfig == null) correlatedEventConfig = CorrelatedEventConfig.getDefaultInstance();
        return this;
    }

    @Override
    public ExternalEventNodeOutput withCorrelatedEventConfig(CorrelatedEventConfig config) {
        this.correlatedEventConfig = config;
        return this;
    }

    public CorrelatedEventConfig getCorrelatedEventConfig() {
        return correlatedEventConfig;
    }

    @Override
    public String getExternalEventDefName() {
        return externalEventDefName;
    }

    @Override
    public PutExternalEventDefRequest toPutExtDefRequest() {
        PutExternalEventDefRequest.Builder builder = PutExternalEventDefRequest.newBuilder()
                .setName(externalEventDefName)
                .setContentType(BuilderUtil.javaTypeToReturnType(payloadClass));

        if (correlatedEventConfig != null) {
            builder.setCorrelatedEventConfig(correlatedEventConfig);
        }
        return builder.build();
    }
}
