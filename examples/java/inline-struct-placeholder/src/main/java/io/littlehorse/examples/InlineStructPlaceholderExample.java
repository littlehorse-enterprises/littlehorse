package io.littlehorse.examples;

import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.InlineStructDef;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.PutStructDefRequest;
import io.littlehorse.sdk.common.proto.RunWfRequest;
import io.littlehorse.sdk.common.proto.StructDefCompatibilityType;
import io.littlehorse.sdk.common.proto.StructFieldDef;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.wfsdk.NodeOutput;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskWorker;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InlineStructPlaceholderExample {

    private static final Logger log = LoggerFactory.getLogger(InlineStructPlaceholderExample.class);
    private static final String WF_SPEC_NAME = "example-inline-struct-placeholder";

    private static final String COMPANY = "acme";
    private static final String CUSTOMER_STRUCT = "customer-acme";

    public static Properties getConfigProps() throws IOException {
        Properties props = new Properties();
        File configPath = Path.of(System.getProperty("user.home"), ".config/littlehorse.config")
                .toFile();
        if (configPath.exists()) {
            props.load(new FileInputStream(configPath));
        }
        return props;
    }

    public static Map<String, String> getPlaceholders() {
        return Map.of("company", COMPANY, "customerStructName", CUSTOMER_STRUCT);
    }

    public static Workflow getWorkflow() {
        String createTaskName = COMPANY + "-create-customer";
        String emailTaskName = COMPANY + "-email-customer";

        return new WorkflowImpl(WF_SPEC_NAME, wf -> {
            WfRunVariable name = wf.declareStr("name").required();
            WfRunVariable email = wf.declareStr("email").required();
            WfRunVariable message = wf.declareStr("message").required();

            NodeOutput customer = wf.execute(createTaskName, name, email);
            wf.execute(emailTaskName, customer, message);
        });
    }

    public static List<LHTaskWorker> getTaskWorkers(LHConfig config) {
        MyWorker executable = new MyWorker();
        Map<String, String> placeholders = getPlaceholders();

        List<LHTaskWorker> workers = List.of(
                new LHTaskWorker(executable, "${company}-create-customer", config, placeholders),
                new LHTaskWorker(executable, "${company}-email-customer", config, placeholders));

        Runtime.getRuntime()
                .addShutdownHook(new Thread(() -> workers.forEach(worker -> {
                    log.debug("Closing {}", worker.getTaskDefName());
                    worker.close();
                })));

        return workers;
    }

    private static void registerStructDef(LittleHorseBlockingStub client) {
        PutStructDefRequest request = PutStructDefRequest.newBuilder()
                .setName(CUSTOMER_STRUCT)
                .setDescription("Customer StructDef used by placeholder example")
                .setAllowedUpdates(StructDefCompatibilityType.NO_SCHEMA_UPDATES)
                .setStructDef(InlineStructDef.newBuilder()
                        .putFields(
                                "id",
                                StructFieldDef.newBuilder()
                                        .setFieldType(
                                                TypeDefinition.newBuilder().setPrimitiveType(VariableType.STR))
                                        .build())
                        .putFields(
                                "name",
                                StructFieldDef.newBuilder()
                                        .setFieldType(
                                                TypeDefinition.newBuilder().setPrimitiveType(VariableType.STR))
                                        .build())
                        .putFields(
                                "email",
                                StructFieldDef.newBuilder()
                                        .setFieldType(
                                                TypeDefinition.newBuilder().setPrimitiveType(VariableType.STR))
                                        .build())
                        .build())
                .build();

        client.putStructDef(request);
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

        registerStructDef(config.getBlockingStub());

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
            throw new IllegalArgumentException("Expected args: <name> <email> <message>");
        }

        LHConfig config = new LHConfig(getConfigProps());
        LittleHorseBlockingStub client = config.getBlockingStub();

        String wfRunId = client.runWf(RunWfRequest.newBuilder()
                        .setWfSpecName(WF_SPEC_NAME)
                        .putVariables("name", LHLibUtil.objToVarVal(args[0]))
                        .putVariables("email", LHLibUtil.objToVarVal(args[1]))
                        .putVariables("message", LHLibUtil.objToVarVal(args[2]))
                        .build())
                .getId()
                .getId();

        System.out.println("Started workflow run: " + wfRunId);
    }
}
