package io.littlehorse.examples;

import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.sdk.worker.LHType;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArrayWorker {
    private static final Logger log = LoggerFactory.getLogger(ArrayWorker.class);

    @LHTaskMethod(value = "produce-array")
    @LHType(isLHArray = true)
    public Long[] produceArray() {
        log.info("Producing native LHArray<Long>");
        return List.of(1L, 2L, 3L).toArray(new Long[0]);
    }

    @LHTaskMethod("consume-array")
    public String consumeArray(@LHType(isLHArray = true) Long[] arr) {
        log.info("Consuming LHArray: {}", List.of(arr).toString());
        return "consumed:" + List.of(arr).toString();
    }
}
