package io.littlehorse.canary.metronome;

import static io.littlehorse.canary.metronome.MetronomeWorkflow.CANARY_WORKFLOW;
import static io.littlehorse.canary.metronome.MetronomeWorkflow.VARIABLE_NAME;
import static io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;

import com.google.protobuf.util.Timestamps;
import io.littlehorse.canary.kafka.MessageEmitter;
import io.littlehorse.canary.proto.BeatKey;
import io.littlehorse.canary.proto.BeatValue;
import io.littlehorse.canary.proto.LatencyBeat;
import io.littlehorse.canary.proto.LatencyBeatKey;
import io.littlehorse.canary.util.Shutdown;
import io.littlehorse.sdk.common.proto.RunWfRequest;
import io.littlehorse.sdk.common.proto.VariableValue;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Metronome {

    public static final String RUN_WF_LATENCY_METRIC_NAME = "run_wf_latency";
    private final MessageEmitter emitter;
    private final ScheduledExecutorService mainExecutor;
    private final ExecutorService requestsExecutor;
    private final int runs;
    private final LittleHorseBlockingStub lhClient;
    private final String serverHost;
    private final int serverPort;
    private final String serverVersion;

    public Metronome(
            final MessageEmitter emitter,
            final LittleHorseBlockingStub lhClient,
            final long frequency,
            final int threads,
            final int runs,
            final String serverHost,
            final int serverPort,
            final String serverVersion) {
        this.emitter = emitter;
        this.runs = runs;
        this.lhClient = lhClient;
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        this.serverVersion = serverVersion;

        mainExecutor = Executors.newSingleThreadScheduledExecutor();
        Shutdown.addShutdownHook("Metronome: Main Executor Thread", () -> closeExecutor(mainExecutor));
        mainExecutor.scheduleAtFixedRate(this::scheduledRun, 0, frequency, TimeUnit.MILLISECONDS);

        requestsExecutor = Executors.newFixedThreadPool(threads);
        Shutdown.addShutdownHook("Metronome: Request Executor Thread", () -> closeExecutor(requestsExecutor));
    }

    private void closeExecutor(final ExecutorService executor) throws InterruptedException {
        executor.shutdownNow();
        executor.awaitTermination(1, TimeUnit.SECONDS);
    }

    private void executeRun() {
        final String wfId = UUID.randomUUID().toString().replace("-", "");
        log.trace("Executing run {}", wfId);

        final Instant before = Instant.now();

        lhClient.runWf(RunWfRequest.newBuilder()
                .setWfSpecName(CANARY_WORKFLOW)
                .setId(wfId)
                .setRevision(0)
                .setMajorVersion(0)
                .putVariables(
                        VARIABLE_NAME,
                        VariableValue.newBuilder()
                                .setInt(Instant.now().toEpochMilli())
                                .build())
                .build());

        final Duration latency = Duration.between(before, Instant.now());

        final BeatKey key = BeatKey.newBuilder()
                .setServerHost(serverHost)
                .setServerPort(serverPort)
                .setServerVersion(serverVersion)
                .setLatencyBeatKey(LatencyBeatKey.newBuilder().setId(RUN_WF_LATENCY_METRIC_NAME))
                .build();

        final BeatValue beat = BeatValue.newBuilder()
                .setTime(Timestamps.now())
                .setLatencyBeat(LatencyBeat.newBuilder().setLatency(latency.toMillis()))
                .build();

        emitter.future(key, beat);
    }

    private void scheduledRun() {
        log.trace("Executing metronome");
        for (int i = 0; i < runs; i++) {
            requestsExecutor.submit(this::executeRun);
        }
    }
}
