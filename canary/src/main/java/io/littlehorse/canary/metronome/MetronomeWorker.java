package io.littlehorse.canary.metronome;

import io.littlehorse.canary.metronome.internal.BeatProducer;
import io.littlehorse.canary.proto.BeatStatus;
import io.littlehorse.canary.proto.BeatType;
import io.littlehorse.canary.util.ShutdownHook;
import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.sdk.worker.LHTaskWorker;
import io.littlehorse.sdk.worker.WorkerContext;
import java.time.Duration;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MetronomeWorker {

    private final BeatProducer producer;

    public MetronomeWorker(final BeatProducer producer, final LHConfig lhConfig) {
        this.producer = producer;

        final LHTaskWorker worker = new LHTaskWorker(this, MetronomeWorkflow.TASK_NAME, lhConfig);
        ShutdownHook.add("Metronome: LH Task Worker", worker);
        worker.registerTaskDef();
        worker.start();

        log.info("Worker Started");
    }

    @LHTaskMethod(MetronomeWorkflow.TASK_NAME)
    public void executeTask(final long startTime, final WorkerContext context) {
        final String id = "%s/%s".formatted(context.getIdempotencyKey(), context.getAttemptNumber());
        log.debug("Executing task {} {}", MetronomeWorkflow.TASK_NAME, id);
        producer.send(
                id,
                BeatType.TASK_RUN_EXECUTION,
                BeatStatus.OK,
                Duration.between(Instant.ofEpochMilli(startTime), Instant.now()));
    }
}
