package io.littlehorse.examples;

import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskWorker;
import java.util.List;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArrayExample {
    private static final Logger log = LoggerFactory.getLogger(ArrayExample.class);

    public static Workflow getWorkflow() {
        return new WorkflowImpl("example-arrays", wf -> {
            // declare a required JSON_ARR input variable
            WfRunVariable inputArray = wf.declareJsonArr("input-array").required();

            // set an INT counter to the size of the input array using the size() operation
            WfRunVariable counter = wf.declareInt("counter");
            counter.assign(inputArray.size());

            // for-loop style while loop: run a task and decrement the counter while it is > 0
            wf.doWhile(counter.isGreaterThan(0), loop -> {
                loop.execute("consume-array", counter);
                counter.assign(counter.subtract(1));
            });
        });
    }

    public static List<LHTaskWorker> getWorkers(LHConfig config) {
        ArrayWorker worker = new ArrayWorker();
        return List.of(new LHTaskWorker(worker, "consume-array", config));
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
