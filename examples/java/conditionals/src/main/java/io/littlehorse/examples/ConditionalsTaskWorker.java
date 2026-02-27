package io.littlehorse.examples;

import io.littlehorse.sdk.worker.LHTaskMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConditionalsTaskWorker {

    private static final Logger log = LoggerFactory.getLogger(ConditionalsTaskWorker.class);

    @LHTaskMethod("send-special-welcome")
    public String taskA() {
        log.info("Special welcome for A");
        return "hello there A";
    }

    @LHTaskMethod("use-expedited-shipping")
    public String taskB() {
        log.debug("Expedited shipping for B");
        return "hello there B";
    }

    @LHTaskMethod("regular-shipping")
    public String taskC() {
        log.debug("Regular shipping for C");
        return "hello there C";
    }

    @LHTaskMethod("task-d")
    public String taskD() {
        log.debug("Executing task-d");
        return "hello there D";
    }
}
