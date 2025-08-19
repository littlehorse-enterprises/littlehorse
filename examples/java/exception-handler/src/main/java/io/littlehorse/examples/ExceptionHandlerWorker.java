package io.littlehorse.examples;

import io.littlehorse.sdk.worker.LHTaskMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExceptionHandlerWorker {

    private static final Logger log = LoggerFactory.getLogger(
        ExceptionHandlerWorker.class
    );

    @LHTaskMethod("fail")
    public String fail() {
        log.debug("Executing fail");
        if (Math.random() > 0.5) {
            log.error("There was an error in this task");
            throw new RuntimeException("Yikes");
        }
        return "hi there";
    }

    @LHTaskMethod("my-task")
    public String passingTask() {
        log.debug("Executing my-task");
        return "woohoo!";
    }
}
