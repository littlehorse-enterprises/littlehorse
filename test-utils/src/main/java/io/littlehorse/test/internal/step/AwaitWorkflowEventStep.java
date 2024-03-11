package io.littlehorse.test.internal.step;

import io.littlehorse.sdk.common.proto.AwaitWorkflowEventRequest;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.WorkflowEvent;
import io.littlehorse.sdk.common.proto.WorkflowEventDefId;
import io.littlehorse.test.internal.TestExecutionContext;
import java.util.function.Consumer;

public class AwaitWorkflowEventStep extends AbstractStep {

    private final String workflowEventDefName;
    private final Consumer<WorkflowEvent> verifier;

    public AwaitWorkflowEventStep(String workflowEventDefName, Consumer<WorkflowEvent> verifier, int id) {
        super(id);
        this.workflowEventDefName = workflowEventDefName;
        this.verifier = verifier;
    }

    @Override
    public void tryExecute(TestExecutionContext context, LittleHorseBlockingStub client) {
        verifier.accept(client.awaitWorkflowEvent(AwaitWorkflowEventRequest.newBuilder()
                .setWfRunId(context.getWfRunId())
                .addEventDefIds(WorkflowEventDefId.newBuilder().setName(workflowEventDefName))
                .build()));
    }
}
