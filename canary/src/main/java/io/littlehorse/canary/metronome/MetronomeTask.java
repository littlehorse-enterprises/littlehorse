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

    public MetronomeTask(MetricsEmitter emitter) {
        this.emitter = emitter;
    }

    @LHTaskMethod(WorkerBootstrap.TASK_NAME)
    public void executeTask(long startTime, WorkerContext context) {
        log.trace("Executing task {}", WorkerBootstrap.TASK_NAME);
        emitTaskRunLatencyMetric(startTime, context);
        emitDuplicatedTaskRunMetric(context);
    }

    private void emitTaskRunLatencyMetric(long startTime, WorkerContext context) {
        long latency =
                Duration.between(Instant.ofEpochMilli(startTime), Instant.now()).toMillis();
        log.debug("Latency {}ms", latency);
        Metric metric = Metric.newBuilder()
                .setTime(Timestamps.fromMillis(System.currentTimeMillis()))
                .setTaskRunLatency(TaskRunLatency.newBuilder().setLatency(latency))
                .build();
        emitter.future(context.getIdempotencyKey(), metric);
    }

    private void emitDuplicatedTaskRunMetric(WorkerContext context) {
        String key = String.format("%s/%s", context.getIdempotencyKey(), context.getAttemptNumber());
        log.debug("Key {}", key);
        Metric metric = Metric.newBuilder()
                .setTime(Timestamps.fromMillis(System.currentTimeMillis()))
                .setDuplicatedTaskRun(DuplicatedTaskRun.newBuilder().setUniqueTaskScheduleId(key))
                .build();
        emitter.emit(key, metric);
    }
}
