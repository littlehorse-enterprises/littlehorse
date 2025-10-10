package io.littlehorse.examples;

import io.littlehorse.sdk.worker.LHTaskMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyWorker {

    private static final Logger log = LoggerFactory.getLogger(MyWorker.class);

    @LHTaskMethod("greet")
    public String greeting(String name) {
        System.out.println("Executing task greet: " + name);
        return "hello there, " + name;
    }

}
