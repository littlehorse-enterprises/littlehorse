package io.littlehorse.examples;

import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc;
import io.littlehorse.sdk.wfsdk.NodeOutput;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskWorker;
import java.util.List;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReturnLHArrayExample {
    private static final Logger log = LoggerFactory.getLogger(ReturnLHArrayExample.class);

    public static Workflow getWorkflow() {
        return new WorkflowImpl("example-return-lharray", wf -> {
            // declare a typed LH Array variable (elements are Long)
            WfRunVariable arrVar = wf.declareArray("my-array", Long.class);

            NodeOutput produced = wf.execute("produce-array");
            arrVar.assign(produced);

            wf.execute("consume-array", arrVar);
        });
    }

    public static List<LHTaskWorker> getWorkers(LHConfig config) {
        LHArrayWorker worker = new LHArrayWorker();
        return List.of(
                new LHTaskWorker(worker, "produce-array", config), new LHTaskWorker(worker, "consume-array", config));
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
