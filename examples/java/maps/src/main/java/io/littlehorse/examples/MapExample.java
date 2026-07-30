package io.littlehorse.examples;

import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc;
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
        return new WorkflowImpl("example-maps", wf -> {
            WfRunVariable mapVar = wf.declareMap("my-map", String.class, Long.class)
                    .withDefault(Map.of("apples", 3L, "bananas", 5L, "cherries", 12L));

            wf.execute("consume-map", mapVar);

            // access a single entry by key
            wf.execute("consume-value", mapVar.get("apples"));

            // put a single entry (inserts or overwrites)
            mapVar.put("dragonfruit", 7L);

            // put with a dynamic (variable-sourced) key
            WfRunVariable keyVar = wf.declareStr("key-name").withDefault("elderberry");
            mapVar.put(keyVar, 42L);

            // build a map from scratch using MapBuilder
            WfRunVariable builtMap = wf.declareMap("built-map", String.class, Long.class);
            LHMapBuilder builder = wf.buildMap();
            builder.put("x", 10L);
            builder.put("y", 20L);
            builtMap.assign(builder);

            wf.execute("consume-map", mapVar);
        });
    }

    public static List<LHTaskWorker> getWorkers(LHConfig config) {
        MapWorker worker = new MapWorker();
        return List.of(
                new LHTaskWorker(worker, "consume-map", config), new LHTaskWorker(worker, "consume-value", config));
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
