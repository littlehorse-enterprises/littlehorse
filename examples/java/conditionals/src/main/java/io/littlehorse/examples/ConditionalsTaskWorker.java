package io.littlehorse.examples;

import io.littlehorse.sdk.worker.LHTaskMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConditionalsTaskWorker {

    private static final Logger log = LoggerFactory.getLogger(ConditionalsTaskWorker.class);

    @LHTaskMethod("task-a")
    public String taskA() {
        log.debug("Executing task-a");
        return "hello there A";
    }

    @LHTaskMethod("task-b")
    public String taskB() {
        log.debug("Executing task-b");
        return "hello there B";
    }

    @LHTaskMethod("task-c")
    public String taskC() {
        log.debug("Executing task-c");
        return "hello there C";
    }

    @LHTaskMethod("task-d")
    public String taskD() {
        log.debug("Executing task-d");
        return "hello there D";
    }
}
