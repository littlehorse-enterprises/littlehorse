package io.littlehorse.examples;

import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.sdk.worker.LHType;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MapWorker {
    private static final Logger log = LoggerFactory.getLogger(MapWorker.class);

    /**
     * [Feature 1] Receives the whole Map as input and returns the total quantity on hand.
     */
    @LHTaskMethod("summarize-inventory")
    public long summarizeInventory(@LHType(isLHMap = true) Map<String, Long> inventory) {
        long total = inventory.values().stream().mapToLong(Long::longValue).sum();
        log.info("Inventory has {} distinct items, {} total units", inventory.size(), total);
        return total;
    }

    /**
     * [Feature 4] Produces a Map of new stock that gets merged into the inventory Map variable.
     */
    @LHTaskMethod("restock")
    @LHType(isLHMap = true)
    public Map<String, Long> restock() {
        Map<String, Long> newStock = new HashMap<>();
        newStock.put("apples", 10L); // overwrites the existing "apples" entry
        newStock.put("dates", 7L); // adds a brand-new entry
        log.info("Restocking with: {}", newStock);
        return newStock;
    }

    /**
     * [Feature 2 - dynamic key] The key is only known at runtime, so the lookup happens here,
     * inside the Task, using the whole Map plus the key.
     */
    @LHTaskMethod("get-quantity")
    public String getQuantity(@LHType(isLHMap = true) Map<String, Long> inventory, String key) {
        Long quantity = inventory.get(key);
        log.info("Quantity for '{}': {}", key, quantity);
        return key + ":" + quantity;
    }

    /**
     * Runs when the requested key is not present in the inventory Map.
     */
    @LHTaskMethod("report-missing")
    public String reportMissing(String key) {
        log.info("Item '{}' is not in the inventory", key);
        return "missing:" + key;
    }
}
