package io.littlehorse.sdk.wfsdk.internal;

import io.littlehorse.sdk.wfsdk.ExternalEventNodeOutput;

public class ExternalEventNodeOutputImpl extends NodeOutputImpl implements ExternalEventNodeOutput {

    private final String externalEventDefName;

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
        parent.registerExternalEventDef(this, payloadClass);
        return this;
    }

    public String getExternalEventDefName() {
        return externalEventDefName;
    }
}
