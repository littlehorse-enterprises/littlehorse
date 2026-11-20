package io.littlehorse.examples;

import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.RunWfRequest;
import io.littlehorse.sdk.common.proto.WfRun;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.structdefutil.LHStructDefType;
import io.littlehorse.sdk.worker.LHTaskWorker;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;

public class StructOneOfExample {

    public static Workflow getWorkflow(LHConfig config) {
        return Workflow.newWorkflow(
                "notify-customer",
                wf -> {
                    var customer = wf.declareStruct("customer", Customer.class).required();
                    wf.execute("notify-customer", customer);
                },
                config);
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

    public static void main(String[] args) throws IOException {
        LHConfig config = new LHConfig(getConfigProps());
        LHTaskWorker worker = new LHTaskWorker(new NotificationWorker(), "notify-customer", config);
        Runtime.getRuntime().addShutdownHook(new Thread(worker::close));

        config.getBlockingStub().putStructDef(new LHStructDefType(Customer.class).toPutStructDefRequest());
        worker.registerTaskDef();
        getWorkflow(config).registerWfSpec(config.getBlockingStub());

        worker.start();

        // TODO: Support oneofs in LHCTL so we do not have to run the workflows from the example code
        runWf(config, new Customer("Obi-Wan", "obi-wan@jedi.org", null));
        runWf(config, new Customer("Anakin", null, "555-0199"));
    }

    private static void runWf(LHConfig config, Customer customer) {
        WfRun wfRun = config.getBlockingStub()
                .runWf(RunWfRequest.newBuilder()
                        .setWfSpecName("notify-customer")
                        .putVariables("customer", LHLibUtil.objToVarVal(customer))
                        .build());
        System.out.println("Started WfRun: " + wfRun.getId().getId());
    }
}
