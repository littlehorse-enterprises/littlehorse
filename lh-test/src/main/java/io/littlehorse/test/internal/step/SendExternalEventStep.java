package io.littlehorse.test.internal.step;

import io.littlehorse.sdk.common.proto.ExternalEventDefId;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc;
import io.littlehorse.sdk.common.proto.PutExternalEventRequest;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.sdk.common.proto.WfRunId;

public class SendExternalEventStep extends AbstractStep {

    private final String externalEventName;
    private final VariableValue content;

    public SendExternalEventStep(String externalEventName, VariableValue content, int id) {
        super(id);
        this.externalEventName = externalEventName;
        this.content = content;
    }

    @Override
    public void tryExecute(Object context, LittleHorseGrpc.LittleHorseBlockingStub lhClient) {
        PutExternalEventRequest externalEventRequest = PutExternalEventRequest.newBuilder()
                .setWfRunId(WfRunId.newBuilder().setId(context.toString()))
                .setContent(content)
                .setExternalEventDefId(ExternalEventDefId.newBuilder().setName(externalEventName))
                .build();
        lhClient.putExternalEvent(externalEventRequest);
    }
}
