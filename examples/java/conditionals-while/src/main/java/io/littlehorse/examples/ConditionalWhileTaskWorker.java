package io.littlehorse.examples;

import io.littlehorse.sdk.worker.LHTaskMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConditionalWhileTaskWorker {

    private static final Logger log = LoggerFactory.getLogger(ConditionalWhileTaskWorker.class);

    @LHTaskMethod("eating-donut")
    public String eatingDonut(int donutsLeft) {
        int left = donutsLeft - 1;
        String message = "eating donut, " + left + " left";
        log.debug(message);
        return message;
    }
}
