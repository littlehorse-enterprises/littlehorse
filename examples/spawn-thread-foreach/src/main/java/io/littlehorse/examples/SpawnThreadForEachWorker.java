package io.littlehorse.examples;

import io.littlehorse.sdk.worker.LHTaskMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpawnThreadForEachWorker {

    private static final Logger log = LoggerFactory.getLogger(
        SpawnThreadForEachWorker.class
    );

    @LHTaskMethod("task-executor")
    public String taskExecutor(String taskInput) {
        log.info("ok, executing task with " + taskInput);
        return "Executed task with input: " + taskInput;
    }
}
