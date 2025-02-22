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

    public MetronomeWorkflow(final LHClient lhClient, final String workflowName, final Duration workflowRetention) {
        final Workflow workflow = Workflow.newWorkflow(
                workflowName,
                thread -> thread.execute(
                        TASK_NAME,
                        thread.addVariable(START_TIME_VARIABLE, VariableType.INT),
                        thread.addVariable(SAMPLE_ITERATION_VARIABLE, VariableType.BOOL)));

        workflow.withRetentionPolicy(WorkflowRetentionPolicy.newBuilder()
                .setSecondsAfterWfTermination(workflowRetention.getSeconds())
                .build());

        lhClient.registerWorkflow(workflow);

        log.info("Workflow {} Registered", workflowName);
    }
}
