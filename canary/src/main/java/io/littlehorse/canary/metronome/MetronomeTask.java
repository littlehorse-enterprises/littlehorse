package io.littlehorse.canary.metronome;

import com.google.protobuf.util.Timestamps;
import io.littlehorse.canary.kafka.MessageEmitter;
import io.littlehorse.canary.proto.*;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.sdk.worker.WorkerContext;
import java.time.Duration;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class MetronomeTask {

    private final MessageEmitter emitter;
    private final String serverHost;
    private final int serverPort;
    private final String serverVersion;

    public MetronomeTask(
            final MessageEmitter emitter, final String serverHost, final int serverPort, final String serverVersion) {
        this.emitter = emitter;
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        this.serverVersion = serverVersion;
    }

    @LHTaskMethod(MetronomeWorkflow.TASK_NAME)
    public void executeTask(final long startTime, final WorkerContext context) {
        final Instant executionTime = Instant.now();

        log.trace("Executing task {}", MetronomeWorkflow.TASK_NAME);

        final EventKey key = EventKey.newBuilder()
                .setServerHost(serverHost)
                .setServerPort(serverPort)
                .setServerVersion(serverVersion)
                .setId("%s/%s".formatted(context.getIdempotencyKey(), context.getAttemptNumber()))
                .setEventType(EventType.TASK_RUN_EXECUTION)
                .build();

        final EventValue beat = EventValue.newBuilder()
                .setTime(Timestamps.fromMillis(executionTime.toEpochMilli()))
                .setLatency(Duration.between(Instant.ofEpochMilli(startTime), executionTime).toMillis())
                .build();

        emitter.emit(key, beat);
    }
}
