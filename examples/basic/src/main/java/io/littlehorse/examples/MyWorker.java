package io.littlehorse.examples;

import io.littlehorse.sdk.common.exception.LHTaskException;
import io.littlehorse.sdk.worker.LHTaskMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyWorker {

    private static final Logger log = LoggerFactory.getLogger(MyWorker.class);

    @LHTaskMethod("greet")
    public String greeting(String name) {
        log.debug("Executing task greet");
        return "hello there, " + name;
    }

    @LHTaskMethod("business-exception-failure")
    public String taskException() throws LHTaskException {
        throw new LHTaskException("this-is-exception", "");
    }
    @LHTaskMethod("my-handler")
    public String handler() {
        log.debug("Executing task greet");
        return "hello there,";
    }
    @LHTaskMethod("my-task")
    public String task() {
        log.debug("Executing task greet");
        return "hello there, ";
    }
}
