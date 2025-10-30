package io.littlehorse.examples;

import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.sdk.worker.WorkerContext;
import java.time.Duration;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyWorker {

    private static final Logger log = LoggerFactory.getLogger(MyWorker.class);

    @LHTaskMethod("task")
    public void task(long requestTime, WorkerContext ctx) { // The context is always the last parameter in the signature
        Instant start = Instant.ofEpochMilli(requestTime);
        Instant end = Instant.now();

        long lag = Duration.between(start, end).toMillis();

        log.debug("Epochs: start {} end {}", requestTime, end.toEpochMilli());
        log.debug("Started {}, Finished {}. Lag in millis: {}", start, end, lag);
        log.debug(
                "Wf run id '{}'. Task global id '{}'. Attempt number '{}'",
                ctx.getWfRunId(),
                ctx.getTaskRunId().getTaskGuid(),
                ctx.getAttemptNumber());
    }
}
