package io.littlehorse.canary.metronome;

import static io.littlehorse.canary.metronome.MetronomeWorkflow.CANARY_WORKFLOW;
import static io.littlehorse.canary.metronome.MetronomeWorkflow.VARIABLE_NAME;
import static io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;

import io.littlehorse.canary.kafka.MetricsEmitter;
import io.littlehorse.canary.util.Shutdown;
import io.littlehorse.sdk.common.proto.RunWfRequest;
import io.littlehorse.sdk.common.proto.VariableValue;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Metronome {

    private final MetricsEmitter emitter;
    private final ScheduledExecutorService mainExecutor;
    private final ExecutorService requestsExecutor;
    private final int runs;
    private final LittleHorseBlockingStub lhClient;

    public Metronome(
            final MetricsEmitter emitter,
            final LittleHorseBlockingStub lhClient,
            final long frequency,
            final int threads,
            final int runs) {
        this.emitter = emitter;
        this.runs = runs;
        this.lhClient = lhClient;

        mainExecutor = Executors.newScheduledThreadPool(1);
        Shutdown.addShutdownHook("Metronome: Main Executor Thread", () -> {
            mainExecutor.shutdownNow();
            mainExecutor.awaitTermination(1, TimeUnit.SECONDS);
        });
        mainExecutor.scheduleAtFixedRate(this::scheduledRun, 0, frequency, TimeUnit.MILLISECONDS);

        requestsExecutor = Executors.newFixedThreadPool(threads);
        Shutdown.addShutdownHook("Metronome: Request Executor Thread", () -> {
            requestsExecutor.shutdownNow();
            requestsExecutor.awaitTermination(1, TimeUnit.SECONDS);
        });
    }

    private void executeRun() {
        final String wfId = UUID.randomUUID().toString().replace("-", "");
        log.trace("Executing run {}", wfId);

        lhClient.runWf(RunWfRequest.newBuilder()
                .setWfSpecName(CANARY_WORKFLOW)
                .setId(wfId)
                .putVariables(
                        VARIABLE_NAME,
                        VariableValue.newBuilder()
                                .setInt(Instant.now().toEpochMilli())
                                .build())
                .build());
    }

    private void scheduledRun() {
        log.trace("Executing metronome");
        for (int i = 0; i < runs; i++) {
            requestsExecutor.submit(this::executeRun);
        }
    }
}
