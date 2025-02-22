package io.littlehorse.canary.metronome;

import io.littlehorse.canary.littlehorse.LHClient;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.WorkflowRetentionPolicy;
import io.littlehorse.sdk.wfsdk.Workflow;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MetronomeWorkflow {

    public static final String TASK_NAME = "canary-worker-task";
    public static final String START_TIME_VARIABLE = "start-time";
    public static final String SAMPLE_ITERATION_VARIABLE = "sample-iteration";
    private final Workflow workflow;
    private final LHClient lhClient;

    public MetronomeWorkflow(final LHClient lhClient, final String workflowName, final Duration workflowRetention) {
        this.lhClient = lhClient;
        this.workflow = Workflow.newWorkflow(
                        workflowName,
                        thread -> thread.execute(
                                TASK_NAME,
                                thread.addVariable(START_TIME_VARIABLE, VariableType.INT),
                                thread.addVariable(SAMPLE_ITERATION_VARIABLE, VariableType.BOOL)))
                .withRetentionPolicy(WorkflowRetentionPolicy.newBuilder()
                        .setSecondsAfterWfTermination(workflowRetention.getSeconds())
                        .build());
    }

    public void register() {
        lhClient.registerWorkflow(workflow);
        log.info("Workflow {} Registered", workflow.getName());
    }
}
