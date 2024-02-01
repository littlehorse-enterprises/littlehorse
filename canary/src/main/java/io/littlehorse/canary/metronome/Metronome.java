package io.littlehorse.canary.metronome;

import static io.littlehorse.canary.metronome.MetronomeWorkflow.CANARY_WORKFLOW;
import static io.littlehorse.canary.metronome.MetronomeWorkflow.VARIABLE_NAME;
import static io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;

import io.littlehorse.canary.kafka.MetricsEmitter;
import io.littlehorse.sdk.common.proto.RunWfRequest;
import io.littlehorse.sdk.common.proto.VariableValue;
import java.io.Closeable;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Metronome implements Closeable {

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
        mainExecutor.scheduleAtFixedRate(this::scheduledRun, 0, frequency, TimeUnit.MILLISECONDS);

        requestsExecutor = Executors.newFixedThreadPool(threads);
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

    @Override
    public void close() {
        mainExecutor.shutdown();
        requestsExecutor.shutdown();
    }
}
