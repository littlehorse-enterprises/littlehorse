package io.littlehorse.examples;

import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc;
import io.littlehorse.sdk.wfsdk.LHMapBuilder;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskWorker;
import java.util.List;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Inventory example that showcases LittleHorse typed Maps:
 *   1. Passing a whole Map as input to a Task.
 *   2. Reading a single entry in the DSL with map.get(dynamicKey).
 *   3. Branching on map.doesContain(key).
 *   4. Declaring and mutating a Map WfRunVariable.
 */
public class MapExample {
    private static final Logger log = LoggerFactory.getLogger(MapExample.class);

    public static Workflow getWorkflow() {
        return new WorkflowImpl("reserve-inventory", wf -> {
            WfRunVariable inventory = wf.declareMap("inventory", String.class, Integer.class);
            WfRunVariable itemToReserve = wf.declareStr("item-to-reserve").withDefault("apples");
            WfRunVariable quantityToReserve =
                    wf.declareInt("quantity-to-reserve").withDefault(2);
            WfRunVariable availableQuantity = wf.declareInt("available-quantity");
            WfRunVariable remainingQuantity = wf.declareInt("remaining-quantity");

            inventory.assign(wf.execute("get-inventory"));

            availableQuantity.assign(inventory.get(itemToReserve));

            wf.doIf(availableQuantity.isGreaterThanEq(quantityToReserve), ifBody -> {
                        remainingQuantity.assign(availableQuantity.subtract(quantityToReserve));
                        inventory.put(itemToReserve, remainingQuantity);

                        LHMapBuilder reservation = ifBody.buildMap().put(itemToReserve, quantityToReserve);
                        ifBody.execute("reserve-items", reservation);
                        ifBody.execute("save-inventory", inventory);
                    })
                    .doElse(elseBody -> {
                        elseBody.execute("notify-out-of-stock", itemToReserve, quantityToReserve, availableQuantity);
                    });
        });
    }

    public static List<LHTaskWorker> getWorkers(LHConfig config) {
        MapWorker worker = new MapWorker();
        return List.of(
                new LHTaskWorker(worker, "get-inventory", config),
                new LHTaskWorker(worker, "reserve-items", config),
                new LHTaskWorker(worker, "save-inventory", config),
                new LHTaskWorker(worker, "notify-out-of-stock", config));
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
