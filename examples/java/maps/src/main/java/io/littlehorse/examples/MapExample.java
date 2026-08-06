package io.littlehorse.examples;

import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc;
import io.littlehorse.sdk.common.proto.VariableMutationType;
import io.littlehorse.sdk.wfsdk.LHMapBuilder;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskWorker;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MapExample {
    private static final Logger log = LoggerFactory.getLogger(MapExample.class);

    public static Workflow getWorkflow() {
        return new WorkflowImpl("reserve-inventory", wf -> {
            WfRunVariable inventory = wf.declareMap("inventory", String.class, Integer.class)
                    .withDefault(Map.of("apples", 3, "bananas", 5, "cherries", 12));
            WfRunVariable sku = wf.declareStr("sku").withDefault("apples");
            WfRunVariable quantity = wf.declareInt("quantity").withDefault(2);
            WfRunVariable available = wf.declareInt("available");
            WfRunVariable remaining = wf.declareInt("remaining");

            available.assign(wf.execute("check-availability", inventory, sku));

            wf.doIf(available.isGreaterThanEq(quantity), stockAvailable -> {
                        remaining.assign(available.subtract(quantity));
                        LHMapBuilder update = stockAvailable.buildMap();
                        update.put(sku, remaining);
                        stockAvailable.mutate(inventory, VariableMutationType.EXTEND, update);

                        LHMapBuilder reservation = stockAvailable.buildMap().put(sku, quantity);
                        stockAvailable.execute("reserve-items", reservation);
                        stockAvailable.execute("save-inventory", inventory);
                    })
                    .doElse(insufficientStock -> {
                        insufficientStock.execute("notify-out-of-stock", sku, quantity, available);
                    });
        });
    }

    public static List<LHTaskWorker> getWorkers(LHConfig config) {
        MapWorker worker = new MapWorker();
        return List.of(
                new LHTaskWorker(worker, "check-availability", config),
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
