package io.littlehorse.examples;

import io.littlehorse.sdk.common.LHArray;
import io.littlehorse.sdk.worker.LHTaskMethod;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LHArrayWorker {
    private static final Logger log = LoggerFactory.getLogger(LHArrayWorker.class);

    @LHTaskMethod("produce-array")
    public LHArray<Long> produceArray() {
        log.info("Producing native LHArray<Long>");
        return LHArray.of(Arrays.asList(100L, 200L, 300L));
    }

    @LHTaskMethod("consume-array")
    public String consumeArray(LHArray<Long> arr) {
        log.info("Consuming LHArray: {}", arr.asList());
        return "consumed:" + arr.asList().size();
    }
}
