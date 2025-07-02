package io.littlehorse.canary.littlehorse;

import static io.littlehorse.canary.metronome.MetronomeWorkflow.SAMPLE_ITERATION_VARIABLE;
import static io.littlehorse.canary.metronome.MetronomeWorkflow.START_TIME_VARIABLE;

import com.google.common.util.concurrent.ListenableFuture;
import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.*;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseFutureStub;
import io.littlehorse.sdk.wfsdk.Workflow;
import java.time.Instant;

public class LHClient {

    private final LittleHorseFutureStub futureStub;
    private final LittleHorseGrpc.LittleHorseBlockingStub blockingStub;
    private final String workflowName;
    private final int workflowRevision;
    private final int workflowVersion;

    public LHClient(
            final LHConfig lhConfig, final String workflowName, final int workflowVersion, final int workflowRevision) {
        this.futureStub = lhConfig.getFutureStub();
        this.blockingStub = lhConfig.getBlockingStub();
        this.workflowName = workflowName;
        this.workflowRevision = workflowRevision;
        this.workflowVersion = workflowVersion;
    }

    public void registerWorkflow(final Workflow workflow) {
        workflow.registerWfSpec(blockingStub);
    }

    public ListenableFuture<WfRun> runCanaryWf(final String id, final Instant start, final boolean sampleIteration) {
        return futureStub.runWf(RunWfRequest.newBuilder()
                .setWfSpecName(workflowName)
                .setId(id)
                .setRevision(workflowRevision)
                .setMajorVersion(workflowVersion)
                .putVariables(
                        START_TIME_VARIABLE,
                        VariableValue.newBuilder().setInt(start.toEpochMilli()).build())
                .putVariables(
                        SAMPLE_ITERATION_VARIABLE,
                        VariableValue.newBuilder().setBool(sampleIteration).build())
                .build());
    }

    public WfRun getCanaryWfRun(final String id) {
        return blockingStub.getWfRun(WfRunId.newBuilder().setId(id).build());
    }
}
