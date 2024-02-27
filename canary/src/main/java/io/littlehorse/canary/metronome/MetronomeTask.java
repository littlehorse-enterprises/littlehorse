package io.littlehorse.canary.metronome;

import com.google.protobuf.util.Timestamps;
import io.littlehorse.canary.kafka.MetricsEmitter;
import io.littlehorse.canary.proto.*;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.sdk.worker.WorkerContext;
import java.time.Duration;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class MetronomeTask {

    public static final String TASK_RUN_LATENCY_METRIC_NAME = "task_run_latency";
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

    private BeatKey.Builder getBeatKeyBuilder() {
        return BeatKey.newBuilder()
                .setServerHost(serverHost)
                .setServerPort(serverPort)
                .setServerVersion(serverVersion);
    }

    private Beat.Builder getBeatBuilder() {
        return Beat.newBuilder().setTime(Timestamps.now());
    }

    @LHTaskMethod(MetronomeWorkflow.TASK_NAME)
    public void executeTask(final long startTime, final WorkerContext context) {
        log.trace("Executing task {}", MetronomeWorkflow.TASK_NAME);
        emitTaskRunLatencyMetric(startTime);
        emitDuplicatedTaskRunMetric(context);
    }

    private void emitTaskRunLatencyMetric(final long startTime) {
        final Duration latency = Duration.between(Instant.ofEpochMilli(startTime), Instant.now());

        final BeatKey key = getBeatKeyBuilder()
                .setLatencyBeatKey(LatencyBeatKey.newBuilder().setName(TASK_RUN_LATENCY_METRIC_NAME))
                .build();
        final Beat beat = getBeatBuilder()
                .setLatencyBeat(LatencyBeat.newBuilder().setLatency(latency.toMillis()))
                .build();

        emitter.future(key, beat);
    }

    private void emitDuplicatedTaskRunMetric(final WorkerContext context) {
        final BeatKey key = getBeatKeyBuilder()
                .setTaskRunBeatKey(TaskRunBeatKey.newBuilder()
                        .setIdempotencyKey(context.getIdempotencyKey())
                        .setAttemptNumber(context.getAttemptNumber()))
                .build();
        final Beat beat = getBeatBuilder()
                .setTaskRunBeat(TaskRunBeat.newBuilder()
                        .setExecutedTime(Timestamps.now())
                        .setScheduledTime(Timestamps.fromDate(context.getScheduledTime())))
                .build();

        // TODO: WHAT HAPPEN IF THIS FAILS?
        emitter.emit(key, beat);
    }
}
