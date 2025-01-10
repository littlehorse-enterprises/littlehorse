package io.littlehorse.canary.util;

import static io.littlehorse.canary.metronome.MetronomeWorkflow.SAMPLE_ITERATION_VARIABLE;
import static io.littlehorse.canary.metronome.MetronomeWorkflow.START_TIME_VARIABLE;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.Empty;
import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.*;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseFutureStub;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import java.time.Instant;
import lombok.NonNull;

public class LHClient implements MeterBinder {

    private final LittleHorseFutureStub futureStub;
    private final LittleHorseGrpc.LittleHorseBlockingStub blockingStub;
    private final String workflowName;
    private final int workflowRevision;
    private final int workflowVersion;
    private static final String WF_RUN_COUNTER_NAME = "canary_metronome_wf_run";
    private final CounterMetric wfRunCounter = new CounterMetric(WF_RUN_COUNTER_NAME);

    public LHClient(
            final LHConfig lhConfig, final String workflowName, final int workflowVersion, final int workflowRevision) {
        this.futureStub = lhConfig.getFutureStub();
        this.blockingStub = lhConfig.getBlockingStub();
        this.workflowName = workflowName;
        this.workflowRevision = workflowRevision;
        this.workflowVersion = workflowVersion;
    }

    public String getServerVersion() {
        final ServerVersion response = blockingStub.getServerVersion(Empty.getDefaultInstance());
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

    @Override
    public void bindTo(@NonNull final MeterRegistry registry) {
        wfRunCounter.bindTo(registry);
    }

    public void incrementWfRunCountMetric() {
        wfRunCounter.increment();
    }

    private static class CounterMetric implements MeterBinder {
        private final String metricName;
        private Counter counter;

        private CounterMetric(final String metricName) {
            this.metricName = metricName;
        }

        @Override
        public void bindTo(@NonNull final MeterRegistry registry) {
            counter = Counter.builder(metricName).register(registry);
        }

        public void increment() {
            if (counter != null) {
                counter.increment();
            }
        }
    }
}
