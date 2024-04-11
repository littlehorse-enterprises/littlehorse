package io.littlehorse.canary.util;

import static io.littlehorse.canary.metronome.MetronomeWorkflow.VARIABLE_NAME;

import com.google.protobuf.Empty;
import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.*;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.wfsdk.Workflow;
import java.time.Instant;

public class LHClient {

    private final LittleHorseBlockingStub blockingStub;
    private final String workflowName;
    private final int workflowRevision;
    private final int workflowVersion;

    public LHClient(
            final LHConfig lhConfig, final String workflowName, final int workflowVersion, final int workflowRevision) {
        blockingStub = lhConfig.getBlockingStub();
        this.workflowName = workflowName;
        this.workflowRevision = workflowRevision;
        this.workflowVersion = workflowVersion;
    }

    public String getServerVersion() {
        final ServerVersionResponse response = blockingStub.getServerVersion(Empty.getDefaultInstance());
        return "%s.%s.%s%s"
                .formatted(
                        response.getMajorVersion(),
                        response.getMinorVersion(),
                        response.getPatchVersion(),
                        response.hasPreReleaseIdentifier() ? "-" + response.getPreReleaseIdentifier() : "");
    }

    public void registerWorkflow(final Workflow workflow) {
        workflow.registerWfSpec(blockingStub);
    }

    public WfRun runCanaryWf(final String id, final Instant start) {
        return blockingStub.runWf(RunWfRequest.newBuilder()
                .setWfSpecName(workflowName)
                .setId(id)
                .setRevision(workflowRevision)
                .setMajorVersion(workflowVersion)
                .putVariables(
                        VARIABLE_NAME,
                        VariableValue.newBuilder().setInt(start.toEpochMilli()).build())
                .build());
    }
}
