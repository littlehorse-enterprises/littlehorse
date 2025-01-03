package io.littlehorse.examples;

import io.littlehorse.sdk.worker.LHTaskMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyWorker2 {
    private static final Logger log = LoggerFactory.getLogger(MyWorker2.class);

    @LHTaskMethod("dismiss")
    public String dismissal(String name) {
        log.debug("Executing task dismiss");
        return "goodbye, " + name;
    }
}