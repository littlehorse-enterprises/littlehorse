package io.littlehorse.examples;

import io.littlehorse.sdk.worker.LHTaskMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyWorker {

    private static final Logger log = LoggerFactory.getLogger(MyWorker.class);

    @LHTaskMethod("increment")
    public Double increment(Double currentValue) {
        log.debug("Executing task increment");
        log.info(String.valueOf(currentValue));
        return ++currentValue;
    }

}
