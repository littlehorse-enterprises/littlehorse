package io.littlehorse.test.internal.step;

import io.littlehorse.sdk.common.proto.LHPublicApiGrpc;
import io.littlehorse.sdk.common.proto.PutExternalEventRequest;
import io.littlehorse.sdk.common.proto.VariableValue;

public class SendExternalEventStep extends AbstractStep {

    private final String externalEventName;
    private final VariableValue content;

    public SendExternalEventStep(String externalEventName, VariableValue content, int id) {
        super(id);
        this.externalEventName = externalEventName;
        this.content = content;
    }

    @Override
    public void tryExecute(Object context, LHPublicApiGrpc.LHPublicApiBlockingStub lhClient) {
        PutExternalEventRequest externalEventRequest = PutExternalEventRequest.newBuilder()
                .setWfRunId(context.toString())
                .setContent(content)
                .setExternalEventDefName(externalEventName)
                .build();
        lhClient.putExternalEvent(externalEventRequest);
    }
}
