package io.littlehorse.canary.metronome;

import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.sdk.worker.WorkerContext;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class WorkerTask {
    @LHTaskMethod(WorkerBootstrap.TASK_NAME)
    public long executeTask(long startTime, WorkerContext context) {
        log.trace("Executing task {}", WorkerBootstrap.TASK_NAME);
        return Instant.now().toEpochMilli();
    }
}
