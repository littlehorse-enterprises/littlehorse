package io.littlehorse.examples;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.littlehorse.sdk.worker.LHTaskMethod;

public class ChildThreadWorker {

    private static final Logger log = LoggerFactory.getLogger(ChildThreadWorker.class);

    @LHTaskMethod("parent-task-1")
    public int parentTask1(int input) {
        log.debug("Executing parent-task-1");
        return input * 2;
    }

    @LHTaskMethod("child-task-1")
    public int childTask1(int input) {
        log.debug("Executing child-task-1");
        return input + 1;
    }

    @LHTaskMethod("child-task-2")
    public String childTask2() {
        log.debug("Executing child-task-2");
        return "child done";
    }

    @LHTaskMethod("grandchild-task")
    public String grandchildTask(int input) {
        log.debug("Executing grandchild-task");
        return "grandchild received: " + input;
    }

    @LHTaskMethod("parent-task-2")
    public String parentTask2() {
        log.debug("Executing parent-task-2");
        return "hello, there!";
    }
}
