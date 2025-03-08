package io.littlehorse.canary.metronome;

import io.littlehorse.canary.infra.HealthStatusBinder;
import io.littlehorse.canary.infra.HealthStatusRegistry;
import io.littlehorse.canary.infra.ShutdownHook;
import io.littlehorse.canary.metronome.internal.BeatProducer;
import io.littlehorse.canary.metronome.model.Beat;
import io.littlehorse.canary.proto.BeatType;
import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.sdk.worker.LHTaskWorker;
import io.littlehorse.sdk.worker.WorkerContext;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ExecutionException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MetronomeWorker implements HealthStatusBinder {

    private final BeatProducer producer;
    private final LHTaskWorker worker;

    public MetronomeWorker(final BeatProducer producer, final LHConfig lhConfig) {
        this.producer = producer;
        this.worker = new LHTaskWorker(this, MetronomeWorkflow.TASK_NAME, lhConfig);
        ShutdownHook.add("Metronome: LH Task Worker", worker);
    }

    public void start() {
        worker.registerTaskDef();
        worker.start();
        log.info("Worker Started");
    }

    @LHTaskMethod(MetronomeWorkflow.TASK_NAME)
    public void executeTask(final long startTime, final boolean sampleIteration, final WorkerContext context)
            throws ExecutionException, InterruptedException {
        log.debug(
                "Executing task {} {}/{}",
                MetronomeWorkflow.TASK_NAME,
                context.getIdempotencyKey(),
                context.getAttemptNumber());

        if (sampleIteration) {
            final String id = "%s/%s".formatted(context.getIdempotencyKey(), context.getAttemptNumber());
            final Duration latency = Duration.between(Instant.ofEpochMilli(startTime), Instant.now());
            sendBeat(id, latency);
        }
    }

    private void sendBeat(final String id, final Duration latency) throws InterruptedException, ExecutionException {
        final Beat beat = Beat.builder(BeatType.TASK_RUN_EXECUTION)
                .id(id)
                .latency(latency)
                .build();
        producer.send(beat).get();
    }

    @Override
    public void bindTo(final HealthStatusRegistry registry) {
        registry.addStatus("metronome-worker", () -> worker.healthStatus().isHealthy());
    }
}
