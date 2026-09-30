package io.littlehorse.examples;

import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.sdk.worker.WorkerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HeartbeatWorker {

    private static final Logger log = LoggerFactory.getLogger(HeartbeatWorker.class);

    @LHTaskMethod(value = "endless-heartbeat", description = "Logs a heartbeat for an endless WfRun.")
    public String heartbeat(int iteration, WorkerContext context) {
        String message = "WfRun " + context.getWfRunId().getId() + " heartbeat #" + iteration;
        log.info(message);
        return message;
    }
}
