package io.littlehorse.examples;

import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.RunWfRequest;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.wfsdk.internal.structdefutil.LHStructDefType;
import io.littlehorse.sdk.worker.LHTaskWorker;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

public class OneOfExample {
    private static final String WF_SPEC_NAME = "oneof-payment";

    public static Workflow getWorkflow() {
        return new WorkflowImpl(WF_SPEC_NAME, wf -> {
            WfRunVariable payment =
                    wf.declareStruct("payment", FlatPayment.class).required();
            wf.execute("describe-payment-fields", payment);
            wf.execute("describe-payment-helper", payment);
        });
    }

    public static void main(String[] args) throws Exception {
        LHConfig config = new LHConfig(getConfigProps());
        if (args.length == 0) {
            runWorkers(config);
        } else {
            runWorkflow(config, args[0]);
        }
    }

    public static Properties getConfigProps() throws IOException {
        Properties props = new Properties();
        Path configPath = Path.of(System.getProperty("user.home"), ".config/littlehorse.config");
        if (Files.exists(configPath)) {
            try (var input = Files.newInputStream(configPath)) {
                props.load(input);
            }
        }
        return props;
    }

    public static void runWorkers(LHConfig config) {
        var client = config.getBlockingStub();
        // Both Java models describe the same named StructDef, so register it once.
        client.putStructDef(new LHStructDefType(FlatPayment.class).toPutStructDefRequest());

        OneOfPaymentWorker executable = new OneOfPaymentWorker();
        List<LHTaskWorker> workers = List.of(
                new LHTaskWorker(executable, "describe-payment-fields", config),
                new LHTaskWorker(executable, "describe-payment-helper", config));
        Runtime.getRuntime().addShutdownHook(new Thread(() -> workers.forEach(LHTaskWorker::close)));

        for (LHTaskWorker worker : workers) {
            worker.registerTaskDef();
        }
        getWorkflow().registerWfSpec(client);
        workers.forEach(LHTaskWorker::start);
    }

    public static void runWorkflow(LHConfig config, String style) {
        Object payment =
                switch (style) {
                    case "fields" -> new FlatPayment("order-123", "tok_abc", null, null);
                    case "helper" -> new NestedPayment("order-123", new PaymentMethod("tok_abc", null, null));
                    default -> throw new IllegalArgumentException("Expected declaration style: fields or helper");
                };

        var run = config.getBlockingStub()
                .runWf(RunWfRequest.newBuilder()
                        .setWfSpecName(WF_SPEC_NAME)
                        .putVariables("payment", LHLibUtil.objToVarVal(payment))
                        .build());
        System.out.println("Started workflow run: " + run.getId().getId());
    }
}
