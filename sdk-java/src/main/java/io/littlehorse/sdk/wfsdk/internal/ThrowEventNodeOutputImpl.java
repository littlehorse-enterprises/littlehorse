package io.littlehorse.sdk.wfsdk.internal;

import io.littlehorse.sdk.common.proto.PutWorkflowEventDefRequest;
import io.littlehorse.sdk.wfsdk.ThrowEventNodeOutput;

public class ThrowEventNodeOutputImpl implements ThrowEventNodeOutput {

    private final WorkflowThreadImpl parent;
    private final String workflowEventDefName;
    private Class<?> payloadClass;

    public ThrowEventNodeOutputImpl(String workflowEventDefName, WorkflowThreadImpl parent) {
        this.workflowEventDefName = workflowEventDefName;
        this.parent = parent;
    }

    @Override
    public void registeredAs(Class<?> payloadClass) {
        this.payloadClass = payloadClass;
        parent.registerWorkflowEventDef(this);
    }

    public PutWorkflowEventDefRequest toPutWorkflowEventDefRequest() {
        return PutWorkflowEventDefRequest.newBuilder()
                .setName(workflowEventDefName)
                .setContentType(BuilderUtil.javaTypeToReturnType(payloadClass))
                .build();
    }
}
