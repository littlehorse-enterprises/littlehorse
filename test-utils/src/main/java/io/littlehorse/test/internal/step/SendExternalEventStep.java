package io.littlehorse.test.internal.step;

import io.littlehorse.sdk.common.proto.ExternalEventDefId;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc;
import io.littlehorse.sdk.common.proto.PutExternalEventRequest;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.test.internal.TestExecutionContext;

public class SendExternalEventStep extends AbstractStep {

    private final String externalEventName;
    private final VariableValue content;

    public SendExternalEventStep(String externalEventName, VariableValue content, int id) {
        super(id);
        this.externalEventName = externalEventName;
        this.content = content;
    }

    @Override
    public void tryExecute(TestExecutionContext context, LittleHorseGrpc.LittleHorseBlockingStub lhClient) {
        PutExternalEventRequest externalEventRequest = PutExternalEventRequest.newBuilder()
                .setWfRunId(context.getWfRunId())
                .setContent(content)
                .setExternalEventDefId(ExternalEventDefId.newBuilder().setName(externalEventName))
                .build();
        lhClient.putExternalEvent(externalEventRequest);
    }
}
