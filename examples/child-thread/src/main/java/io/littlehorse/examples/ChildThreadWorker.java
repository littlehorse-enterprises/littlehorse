package io.littlehorse.examples;

import io.littlehorse.sdk.worker.LHTaskMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChildThreadWorker {

    private static final Logger log = LoggerFactory.getLogger(
        ChildThreadWorker.class
    );

    @LHTaskMethod("parent-task-1")
    public int parentTask1(int input) {
        log.debug("Executing parent-task-1");
        return input * 2;
    }

    @LHTaskMethod("child-task")
    public String childTask(int input) {
        log.debug("Executing child-task");
        return "hi there, input was: " + input;
    }

    @LHTaskMethod("parent-task-2")
    public String parentTask2() {
        log.debug("Executing parent-task-2");
        return "hello, there!";
    }
}
