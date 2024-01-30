package io.littlehorse.canary.metronome;

import com.google.protobuf.util.Timestamps;
import io.littlehorse.canary.kafka.MetricsEmitter;
import io.littlehorse.canary.proto.DuplicatedTaskRun;
import io.littlehorse.canary.proto.Metric;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.sdk.worker.WorkerContext;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class MetronomeTask {

    private final MetricsEmitter emitter;

    public MetronomeTask(MetricsEmitter emitter) {
        this.emitter = emitter;
    }

    @LHTaskMethod(WorkerBootstrap.TASK_NAME)
    public long executeTask(long startTime, WorkerContext context) {
        log.trace("Executing task {}", WorkerBootstrap.TASK_NAME);

        String key = String.format("%s/%s", context.getIdempotencyKey(), context.getAttemptNumber());
        Metric metric = Metric.newBuilder()
                .setTime(Timestamps.fromMillis(System.currentTimeMillis()))
                .setDuplicatedTaskRun(DuplicatedTaskRun.newBuilder().setUniqueTaskScheduleId(key))
                .build();

        emitter.emit(key, metric);
        return Instant.now().toEpochMilli();
    }
}
