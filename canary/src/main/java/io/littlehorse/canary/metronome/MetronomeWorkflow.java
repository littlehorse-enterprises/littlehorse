package io.littlehorse.canary.metronome;

import io.littlehorse.canary.util.LHClient;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.wfsdk.Workflow;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MetronomeWorkflow {

    public static final String TASK_NAME = "canary-worker-task";
    public static final String VARIABLE_NAME = "start-time";

    public MetronomeWorkflow(final LHClient lhClient, final String workflowName) {
        final Workflow workflow = Workflow.newWorkflow(
                workflowName, thread -> thread.execute(TASK_NAME, thread.addVariable(VARIABLE_NAME, VariableType.INT)));
        lhClient.registerWorkflow(workflow);

        log.info("Workflow {} Registered", workflowName);
    }
}
