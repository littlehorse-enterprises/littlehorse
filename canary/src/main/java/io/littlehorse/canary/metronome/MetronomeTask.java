package io.littlehorse.canary.metronome;

import com.google.protobuf.util.Timestamps;
import io.littlehorse.canary.kafka.MetricsEmitter;
import io.littlehorse.canary.proto.Beat;
import io.littlehorse.canary.proto.BeatKey;
import io.littlehorse.canary.proto.TaskRunBeat;
import io.littlehorse.canary.proto.TaskRunBeatKey;
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

    @LHTaskMethod(MetronomeWorkflow.TASK_NAME)
    public void executeTask(final long startTime, final WorkerContext context) {
        final Instant executionTime = Instant.now();

        log.trace("Executing task {}", MetronomeWorkflow.TASK_NAME);

        final BeatKey key = BeatKey.newBuilder()
                .setServerHost(serverHost)
                .setServerPort(serverPort)
                .setServerVersion(serverVersion)
                .setTaskRunBeatKey(TaskRunBeatKey.newBuilder()
                        .setIdempotencyKey(context.getIdempotencyKey())
                        .setAttemptNumber(context.getAttemptNumber()))
                .build();

        final Beat beat = Beat.newBuilder()
                .setTime(Timestamps.now())
                .setTaskRunBeat(TaskRunBeat.newBuilder()
                        .setScheduledTime(Timestamps.fromDate(context.getScheduledTime()))
                        .setRequestedTime(Timestamps.fromMillis(startTime))
                        .setExecutedTime(Timestamps.fromMillis(executionTime.toEpochMilli()))
                        .setLatency(Duration.between(Instant.ofEpochMilli(startTime), executionTime)
                                .toMillis()))
                .build();

        emitter.emit(key, beat);
    }
}
