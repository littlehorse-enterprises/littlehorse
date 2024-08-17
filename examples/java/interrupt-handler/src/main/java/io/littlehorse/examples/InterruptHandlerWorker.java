package io.littlehorse.examples;

import io.littlehorse.sdk.worker.LHTaskMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InterruptHandlerWorker {

    private static final Logger log = LoggerFactory.getLogger(
        InterruptHandlerWorker.class
    );

    @LHTaskMethod("some-task")
    public void someTask() {
        log.warn("Executing some-task");
        throw new RuntimeException("My task has failed");
    }

    @LHTaskMethod("my-task")
    public String myTask() {
        log.debug("Executing my-task");
        return "hello, there!";
    }
}
