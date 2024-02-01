package io.littlehorse.canary.metronome;

import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.wfsdk.Workflow;

public class MetronomeWorkflow {
    public static final String TASK_NAME = "canary-worker-task";
    public static final String VARIABLE_NAME = "start-time";
    public static final String CANARY_WORKFLOW = "canary-workflow";
    private final Workflow workflow;
    private final LittleHorseBlockingStub lhClient;

    public MetronomeWorkflow(final LittleHorseBlockingStub lhClient) {
        workflow = Workflow.newWorkflow(
                CANARY_WORKFLOW,
                thread -> thread.execute(TASK_NAME, thread.addVariable(VARIABLE_NAME, VariableType.INT)));
        this.lhClient = lhClient;
    }

    public void register() {
        workflow.registerWfSpec(lhClient);
    }
}
