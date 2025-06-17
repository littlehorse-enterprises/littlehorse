package io.littlehorse.test.internal.step;

import io.littlehorse.sdk.common.proto.ExternalEventDefId;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc;
import io.littlehorse.sdk.common.proto.PutCorrelatedEventRequest;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.test.internal.TestExecutionContext;

public class SendCorrelatedEventStep extends AbstractStep {

    private final String externalEventName;
    private final String correlationKey;
    private final VariableValue content;

    public SendCorrelatedEventStep(String externalEventName, String correlationKey, VariableValue content, int id) {
        super(id);
        this.externalEventName = externalEventName;
        this.correlationKey = correlationKey;
        this.content = content;
    }

    @Override
    public void tryExecute(TestExecutionContext context, LittleHorseGrpc.LittleHorseBlockingStub lhClient) {
        PutCorrelatedEventRequest req = PutCorrelatedEventRequest.newBuilder()
                .setContent(content)
                .setExternalEventDefId(ExternalEventDefId.newBuilder().setName(externalEventName))
                .setKey(correlationKey)
                .build();
        lhClient.putCorrelatedEvent(req);
    }
}
