package io.littlehorse.test.internal.step;

import io.littlehorse.sdk.common.proto.PutExternalEventRequest;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.test.LHClientTestWrapper;

public class SendExternalEventStep implements Step {

    private final String externalEventName;
    private final VariableValue content;

    public SendExternalEventStep(String externalEventName, VariableValue content) {
        this.externalEventName = externalEventName;
        this.content = content;
    }

    @Override
    public void execute(Object context, LHClientTestWrapper lhClientWrapper) {
        PutExternalEventRequest externalEventRequest = PutExternalEventRequest.newBuilder()
                .setWfRunId(context.toString())
                .setContent(content)
                .setExternalEventDefName(externalEventName)
                .build();
        lhClientWrapper.putExternalEvent(externalEventRequest);
    }
}
