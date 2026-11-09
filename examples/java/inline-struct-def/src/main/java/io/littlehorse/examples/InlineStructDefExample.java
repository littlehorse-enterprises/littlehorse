package io.littlehorse.examples;

import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.PutStructDefRequest;
import io.littlehorse.sdk.common.proto.RunInlineWfRequest;
import io.littlehorse.sdk.common.proto.StructDefCompatibilityType;
import io.littlehorse.sdk.common.proto.WfRun;
import io.littlehorse.sdk.wfsdk.NodeOutput;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.structdefutil.LHStructDefType;
import io.littlehorse.sdk.worker.LHTaskWorker;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InlineStructDefExample {

    private static final Logger log = LoggerFactory.getLogger(InlineStructDefExample.class);

    public static Workflow getWorkflow(LHConfig config) {
        return Workflow.newWorkflow(
                "example-inline-struct-def",
                wf -> {
                    WfRunVariable order = wf.declareStruct("order", Order.class).required();
                    WfRunVariable normalizedOrder = wf.declareStruct("normalized-order", Order.class);

                    normalizedOrder.assign(wf.execute("normalize-order", order));
                    wf.execute("format-shipping-label", normalizedOrder);
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

    public static List<LHTaskWorker> getTaskWorkers(LHConfig config) {
        InlineStructDefWorker executable = new InlineStructDefWorker();
        List<LHTaskWorker> workers = List.of(
                new LHTaskWorker(executable, "normalize-order", config),
                new LHTaskWorker(executable, "format-shipping-label", config));

        Runtime.getRuntime()
                .addShutdownHook(new Thread(() -> workers.forEach(worker -> {
                    log.debug("Closing {}", worker.getTaskDefName());
                    worker.close();
                })));
        return workers;
    }

    private static void registerStructDefs(LittleHorseBlockingStub client, Class<?>... structDefClasses) {
        for (Class<?> structDefClass : structDefClasses) {
            registerStructDef(client, structDefClass);
        }
    }

    private static void registerStructDef(LittleHorseBlockingStub client, Class<?> structDefClass) {
        StructDefCompatibilityType compatibilityType = StructDefCompatibilityType.NO_SCHEMA_UPDATES;

        LHStructDefType structDefType = new LHStructDefType(structDefClass);

        PutStructDefRequest request = structDefType.toPutStructDefRequest().toBuilder()
                .setAllowedUpdates(compatibilityType)
                .build();

        client.putStructDef(request);
    }

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            throw new IllegalArgumentException("Usage: InlineStructDefExample [workers|inline]");
        }
        String mode = args.length == 0 ? "workers" : args[0];
        switch (mode) {
            case "workers" -> runWorkers();
            case "inline" -> runInlineWorkflow();
            default -> throw new IllegalArgumentException("Usage: InlineStructDefExample [workers|inline]");
        }
    }

    public static void runInlineWorkflow() throws IOException {
        LHConfig config = new LHConfig(getConfigProps());
        UUID random = UUID.randomUUID();
        Order order = new Order(
                "ORD-%s".formatted(random.toString()),
                new DeliveryAddress("123 Main Street   ", "        Springfield", "      12345"));
        RunInlineWfRequest request = Workflow.inlineWorkflow(thread -> {
                    NodeOutput normalizedOrder = thread.execute("normalize-order", order);
                    NodeOutput shippingLabel = thread.execute("format-shipping-label", normalizedOrder);
                    thread.complete(shippingLabel);
                })
                .compileWorkflow(config);

        WfRun run = config.getBlockingStub().runInlineWf(request);
        System.out.println("Started inline WfRun: " + run.getId().getId());
    }

    public static void runWorkers() throws IOException {
        LHConfig config = new LHConfig(getConfigProps());
        Workflow workflow = getWorkflow(config);
        List<LHTaskWorker> workers = getTaskWorkers(config);

        // The `order` StructDef embeds the `delivery-address` schema as an InlineStructDef,
        // so only the named `order` StructDef needs to be registered here.
        registerStructDefs(config.getBlockingStub(), Order.class);

        for (LHTaskWorker worker : workers) {
            worker.registerTaskDef();
        }

        workflow.registerWfSpec(config.getBlockingStub());

        for (LHTaskWorker worker : workers) {
            log.info("Starting {}", worker.getTaskDefName());
            worker.start();
        }
    }
}
