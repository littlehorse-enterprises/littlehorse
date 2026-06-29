package io.littlehorse.examples;

import io.littlehorse.sdk.worker.LHTaskMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArrayWorker {
    private static final Logger log = LoggerFactory.getLogger(ArrayWorker.class);

    @LHTaskMethod("consume-array")
    public String consumeArray(int remaining) {
        log.info("Processing array element, {} remaining", remaining);
        return "processed, remaining=" + remaining;
    }
}
