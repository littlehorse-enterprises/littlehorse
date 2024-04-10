package io.littlehorse.canary.util;

import static io.littlehorse.canary.metronome.MetronomeWorkflow.CANARY_WORKFLOW;
import static io.littlehorse.canary.metronome.MetronomeWorkflow.VARIABLE_NAME;

import com.google.protobuf.Empty;
import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.*;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.wfsdk.Workflow;
import java.time.Instant;

public class LHClient {

    private final LittleHorseBlockingStub blockingStub;

    public LHClient(LHConfig lhConfig) {
        blockingStub = lhConfig.getBlockingStub();
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
                .setWfSpecName(CANARY_WORKFLOW)
                .setId(id)
                .putVariables(
                        VARIABLE_NAME,
                        VariableValue.newBuilder().setInt(start.toEpochMilli()).build())
                .build());
    }
}
