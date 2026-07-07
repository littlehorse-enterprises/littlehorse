package io.littlehorse.examples;

import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.sdk.worker.LHType;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MapWorker {
    private static final Logger log = LoggerFactory.getLogger(MapWorker.class);

    @LHTaskMethod(value = "produce-map")
    @LHType(isLHMap = true)
    public Map<String, Long> produceMap() {
        log.info("Producing native LittleHorse Map of type <String, Long>");
        return Map.of("apples", 3L, "bananas", 5L, "cherries", 12L);
    }

    @LHTaskMethod("consume-map")
    public String consumeMap(@LHType(isLHMap = true) Map<String, Long> items) {
        log.info("Consuming LHMap: {}", items);
        return "consumed:" + items.toString();
    }

    @LHTaskMethod("consume-value")
    public String consumeValue(long value) {
        log.info("Consuming single map value: {}", value);
        return "value:" + value;
    }
}
