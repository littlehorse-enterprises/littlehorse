package io.littlehorse.canary.metronome;

import com.google.protobuf.util.Timestamps;
import io.littlehorse.canary.kafka.MetricsEmitter;
import io.littlehorse.canary.proto.DuplicatedTaskRun;
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

    public MetronomeTask(final MetricsEmitter emitter) {
        this.emitter = emitter;
    }

    @LHTaskMethod(MetronomeWorkflow.TASK_NAME)
    public void executeTask(final long startTime, final WorkerContext context) {
        log.trace("Executing task {}", MetronomeWorkflow.TASK_NAME);
        emitTaskRunLatencyMetric(startTime, context);
        emitDuplicatedTaskRunMetric(context);
    }

    private void emitTaskRunLatencyMetric(final long startTime, final WorkerContext context) {
        final long latency =
                Duration.between(Instant.ofEpochMilli(startTime), Instant.now()).toMillis();
        log.debug("Latency {}ms", latency);
        final Metric metric = Metric.newBuilder()
                .setTime(Timestamps.fromMillis(System.currentTimeMillis()))
                .setTaskRunLatency(TaskRunLatency.newBuilder().setLatency(latency))
                .build();
        emitter.future(context.getWfRunId().getId(), metric);
    }

    private void emitDuplicatedTaskRunMetric(final WorkerContext context) {
        final String key = String.format("%s/%s", context.getIdempotencyKey(), context.getAttemptNumber());
        log.debug("Key {}", key);
        final Metric metric = Metric.newBuilder()
                .setTime(Timestamps.fromMillis(System.currentTimeMillis()))
                .setDuplicatedTaskRun(DuplicatedTaskRun.newBuilder().setUniqueTaskScheduleId(key))
                .build();
        emitter.emit(key, metric);
    }
}
