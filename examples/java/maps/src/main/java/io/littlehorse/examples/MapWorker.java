package io.littlehorse.examples;

import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.sdk.worker.LHType;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MapWorker {
    private static final Logger log = LoggerFactory.getLogger(MapWorker.class);

    @LHTaskMethod("get-inventory")
    @LHType(isLHMap = true)
    public Map<String, Integer> getInventory() {
        return Map.of("apples", 3, "bananas", 5, "cherries", 12);
    }

    @LHTaskMethod("reserve-items")
    public void reserveItems(@LHType(isLHMap = true) Map<String, Integer> reservation) {
        log.info("Reserved items: {}", reservation);
    }

    @LHTaskMethod("save-inventory")
    public void saveInventory(@LHType(isLHMap = true) Map<String, Integer> inventory) {
        log.info("Saved inventory: {}", inventory);
    }

    @LHTaskMethod("notify-out-of-stock")
    public void notifyOutOfStock(String sku, int requested, Integer available) {
        log.info("Insufficient stock for {}: requested {}, available {}", sku, requested, available);
    }
}
