package io.littlehorse.canary.metronome;

import io.littlehorse.canary.util.LHClient;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.wfsdk.Workflow;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MetronomeWorkflow {

    public static final String TASK_NAME = "canary-worker-task";
    public static final String VARIABLE_NAME = "start-time";
    public static final String CANARY_WORKFLOW = "canary-workflow";

    public MetronomeWorkflow(final LHClient lhClient) {
        final Workflow workflow = Workflow.newWorkflow(
                CANARY_WORKFLOW,
                thread -> thread.execute(TASK_NAME, thread.addVariable(VARIABLE_NAME, VariableType.INT)));
        lhClient.registerWorkflow(workflow);

        log.info("Workflow {} Registered", CANARY_WORKFLOW);
    }
}
