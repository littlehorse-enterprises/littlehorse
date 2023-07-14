package io.littlehorse.examples;

import io.littlehorse.sdk.worker.LHTaskMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyWorker {

    private static final Logger log = LoggerFactory.getLogger(MyWorker.class);

    @LHTaskMethod("greet0")
    public String greeting0(String name) {
        log.debug("Executing task greet0");
        return "hello there, " + name;
    }

    @LHTaskMethod("greet1")
    public String greeting1(String name) {
        log.debug("Executing task greet1");
        return "hello there, " + name + ", this is the latest version";
    }
}
