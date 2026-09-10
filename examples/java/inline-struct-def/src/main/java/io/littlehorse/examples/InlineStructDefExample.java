package io.littlehorse.examples;

import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.adapter.LHTypeAdapterRegistry;
import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.RunWfRequest;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskWorker;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InlineStructDefExample {

    private static final Logger log = LoggerFactory.getLogger(InlineStructDefExample.class);
    private static final String WF_SPEC_NAME = "example-inline-struct-def";

    public static Workflow getWorkflow() {
        return new WorkflowImpl(WF_SPEC_NAME, wf -> {
            WfRunVariable address =
                    wf.declareInlineStruct("address", DeliveryAddress.class).required();
            WfRunVariable normalizedAddress = wf.declareInlineStruct("normalized-address", DeliveryAddress.class);

            normalizedAddress.assign(wf.execute("normalize-address", address));
            wf.execute("format-shipping-label", normalizedAddress);
        });
    }

    public static Properties getConfigProps() throws IOException {
        Properties props = new Properties();
        File configPath = Path.of(System.getProperty("user.home"), ".config/littlehorse.config")
                .toFile();
        if (configPath.exists()) {
            props.load(new FileInputStream(configPath));
        }
        return props;
    }

    public static List<LHTaskWorker> getTaskWorkers(LHConfig config) {
        InlineStructDefWorker executable = new InlineStructDefWorker();
        List<LHTaskWorker> workers = List.of(
                new LHTaskWorker(executable, "normalize-address", config),
                new LHTaskWorker(executable, "format-shipping-label", config));

        Runtime.getRuntime()
                .addShutdownHook(new Thread(() -> workers.forEach(worker -> {
                    log.debug("Closing {}", worker.getTaskDefName());
                    worker.close();
                })));
        return workers;
    }

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            runWorkers();
        } else {
            runWorkflow(args);
        }
    }

    public static void runWorkers() throws IOException {
        LHConfig config = new LHConfig(getConfigProps());
        Workflow workflow = getWorkflow();
        List<LHTaskWorker> workers = getTaskWorkers(config);

        for (LHTaskWorker worker : workers) {
            worker.registerTaskDef();
        }

        workflow.registerWfSpec(config.getBlockingStub());

        for (LHTaskWorker worker : workers) {
            log.info("Starting {}", worker.getTaskDefName());
            worker.start();
        }
    }

    public static void runWorkflow(String[] args) throws IOException {
        if (args.length < 3) {
            throw new IllegalArgumentException("Expected args: <street> <city> <postal-code>");
        }

        LHConfig config = new LHConfig(getConfigProps());
        LittleHorseBlockingStub client = config.getBlockingStub();
        DeliveryAddress address = new DeliveryAddress(args[0], args[1], args[2]);

        String wfRunId = client.runWf(RunWfRequest.newBuilder()
                        .setWfSpecName(WF_SPEC_NAME)
                        .putVariables(
                                "address",
                                LHLibUtil.objToVarVal(address, DeliveryAddress.class, LHTypeAdapterRegistry.empty()))
                        .build())
                .getId()
                .getId();

        System.out.println("Started workflow run: " + wfRunId);
    }
}
