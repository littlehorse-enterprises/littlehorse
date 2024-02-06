package io.littlehorse.canary.metronome;

import com.google.protobuf.util.Timestamps;
import io.littlehorse.canary.kafka.MetricsEmitter;
import io.littlehorse.canary.proto.DuplicatedTaskRun;
import io.littlehorse.canary.proto.Metadata;
import io.littlehorse.canary.proto.Metric;
import io.littlehorse.canary.proto.TaskRunLatency;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.sdk.worker.WorkerContext;
import java.time.Duration;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class MetronomeTask {

    private final MetricsEmitter emitter;
    private final String serverHost;
    private final int serverPort;
    private final String serverVersion;

    public MetronomeTask(
            final MetricsEmitter emitter, final String serverHost, final int serverPort, final String serverVersion) {
        this.emitter = emitter;
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        this.serverVersion = serverVersion;
    }

    private Metric.Builder getMetricBuilder() {
        return Metric.newBuilder()
                .setMetadata(Metadata.newBuilder()
                        .setTime(Timestamps.fromMillis(System.currentTimeMillis()))
                        .setServerHost(serverHost)
                        .setServerPort(serverPort)
                        .setServerVersion(serverVersion));
    }

    @LHTaskMethod(MetronomeWorkflow.TASK_NAME)
    public void executeTask(final long startTime, final WorkerContext context) {
        log.trace("Executing task {}", MetronomeWorkflow.TASK_NAME);
        emitTaskRunLatencyMetric(startTime, context);
        emitDuplicatedTaskRunMetric(context);
    }

    private void emitTaskRunLatencyMetric(final long startTime, final WorkerContext context) {
        final Duration latency = Duration.between(Instant.ofEpochMilli(startTime), Instant.now());
        final String key = "%s:%s".formatted(serverHost, serverPort);

        final Metric metric = getMetricBuilder()
                .setTaskRunLatency(TaskRunLatency.newBuilder().setLatency(latency.toMillis()))
                .build();

        emitter.future(key, metric);
    }

    private void emitDuplicatedTaskRunMetric(final WorkerContext context) {
        final String key = "%s/%s".formatted(context.getIdempotencyKey(), context.getAttemptNumber());

        final Metric metric = getMetricBuilder()
                .setDuplicatedTaskRun(DuplicatedTaskRun.newBuilder().setUniqueTaskScheduleId(key))
                .build();

        emitter.emit(key, metric);
    }
}
