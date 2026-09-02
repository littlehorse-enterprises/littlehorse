package io.littlehorse.examples;

import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskWorker;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Inventory example that showcases LittleHorse typed Maps:
 *   1. Passing a whole Map as input to a Task.
 *   2. Reading a single entry in the DSL with map.get(literalKey).
 *   3. Branching on map.doesContain(key).
 *   4. Declaring and mutating a Map WfRunVariable.
 */
public class MapExample {
    private static final Logger log = LoggerFactory.getLogger(MapExample.class);

    public static Workflow getWorkflow() {
        return new WorkflowImpl("example-maps", wf -> {
            // A Map WfRunVariable: item name -> quantity on hand.
            WfRunVariable inventory = wf.declareMap("my-map", String.class, Long.class)
                    .withDefault(Map.of("apples", 3L, "bananas", 5L, "cherries", 12L));

            // Which item to look up at runtime (defaults to "apples").
            WfRunVariable lookupKey = wf.declareStr("lookup-key").withDefault("apples");

            // Pass the whole Map into a Task; it returns the total quantity of items on hand.
            WfRunVariable totalCount = wf.declareInt("total-count");
            totalCount.assign(wf.execute("summarize-inventory", inventory));

            // Merge a restock Map into the inventory Map variable.
            inventory.assign(inventory.extend(wf.execute("restock")));

            // Read a single entry by literal key straight from the Map.
            // Note: the DSL get(...) requires a compile-time key.
            WfRunVariable applesQty = wf.declareInt("apples-qty");
            applesQty.assign(inventory.get("apples"));

            wf.doIfElse(
                    inventory.doesContain(lookupKey),
                    ifBody ->
                            // [Feature 2 - dynamic key] The key is only known at runtime, so the
                            // lookup happens inside the Task, which receives the whole Map + key.
                            ifBody.execute("get-quantity", inventory, lookupKey),
                    elseBody -> elseBody.execute("report-missing", lookupKey));
        });
    }

    public static List<LHTaskWorker> getWorkers(LHConfig config) {
        MapWorker worker = new MapWorker();
        return List.of(
                new LHTaskWorker(worker, "summarize-inventory", config),
                new LHTaskWorker(worker, "restock", config),
                new LHTaskWorker(worker, "get-quantity", config),
                new LHTaskWorker(worker, "report-missing", config));
    }

    public static void main(String[] args) throws Exception {
        Properties props = new Properties();
        LHConfig cfg = new LHConfig(props);
        LittleHorseGrpc.LittleHorseBlockingStub client = cfg.getBlockingStub();

        Workflow wf = getWorkflow();
        List<LHTaskWorker> workers = getWorkers(cfg);

        // register task defs
        for (LHTaskWorker w : workers) {
            w.registerTaskDef();
        }

        // register workflow
        wf.registerWfSpec(client);

        // start workers
        for (LHTaskWorker w : workers) {
            log.info("Starting worker {}", w.getTaskDefName());
            w.start();
        }
    }
}
