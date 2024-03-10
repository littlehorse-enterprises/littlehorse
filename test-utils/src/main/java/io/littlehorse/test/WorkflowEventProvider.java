package io.littlehorse.test;

import io.littlehorse.sdk.common.proto.PutWorkflowEventDefRequest;

@FunctionalInterface
public interface WorkflowEventProvider {

    PutWorkflowEventDefRequest get();
}
