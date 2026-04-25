package io.littlehorse.examples;

import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.PutStructDefRequest;
import io.littlehorse.sdk.common.proto.RunWfRequest;
import io.littlehorse.sdk.common.proto.StructDefCompatibilityType;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.sdk.wfsdk.LHStructBuilder;
import io.littlehorse.sdk.wfsdk.NodeOutput;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.wfsdk.internal.structdefutil.LHStructDefType;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.sdk.worker.LHTaskWorker;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Demonstrates building Struct values inside a WfSpec using the
 * {@link LHStructBuilder} and {@code InlineLHStructBuilder} APIs.
 *
 * <p>Instead of receiving a fully-formed Struct from a task output, this
 * example assembles a {@code Person} struct inside the workflow by combining
 * input variables with the output of a task that returns address data.
 */
public class StructBuilderExample {

    private static final Logger log = LoggerFactory.getLogger(StructBuilderExample.class);

    public static Workflow getWorkflow() {
        return new WorkflowImpl("assemble-person", wf -> {
            // Input variables
            WfRunVariable name = wf.declareStr("name").required();
            WfRunVariable email = wf.declareStr("email").required();

            // Internal variable to hold the assembled person
            WfRunVariable personRecord = wf.declareStruct("person-record", Person.class);

            // Fetch the address from an external source
            NodeOutput addressOutput = wf.execute("fetch-address", name);

            // Build the Person struct using buildStruct + buildInlineStruct.
            // This is the key feature: we assemble a Struct inside the WfSpec
            // instead of relying on a task to return a complete Struct.
            LHStructBuilder personStruct = wf.buildStruct("person")
                    .put("name", name)
                    .put("email", email)
                    .put(
                            "address",
                            wf.buildInlineStruct()
                                    .put("street", addressOutput.get("street"))
                                    .put("city", addressOutput.get("city"))
                                    .put("state", addressOutput.get("state"))
                                    .put("zip", addressOutput.get("zip")));

            personRecord.assign(personStruct);

            // Use the assembled Struct
            wf.execute("save-person", personRecord);
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
        MyWorker executable = new MyWorker();

        List<LHTaskWorker> workers = new ArrayList<>();

        for (Method method : executable.getClass().getDeclaredMethods()) {
            if (!method.isAnnotationPresent(LHTaskMethod.class)) continue;

            LHTaskMethod lhTaskMethod = method.getAnnotation(LHTaskMethod.class);

            workers.add(new LHTaskWorker(executable, lhTaskMethod.value(), config));
        }

        // Gracefully shutdown
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
        if (args.length == 0) {
            runWorkers();
        } else {
            runWf(args);
        }
    }

    public static void runWorkers() throws IOException {
        Properties props = getConfigProps();
        LHConfig config = new LHConfig(props);

        Workflow workflow = getWorkflow();

        List<LHTaskWorker> workers = getTaskWorkers(config);

        // Register StructDefs (address must be registered before person)
        registerStructDefs(config.getBlockingStub(), Address.class, Person.class);

        // Register TaskDefs
        for (LHTaskWorker worker : workers) {
            worker.registerTaskDef();
        }

        // Register WfSpec
        workflow.registerWfSpec(config.getBlockingStub());

        // Start task workers
        for (LHTaskWorker worker : workers) {
            log.debug("Starting {}", worker.getTaskDefName());
            worker.start();
        }
    }

    public static void runWf(String[] args) throws IOException {
        if (args.length < 2) {
            throw new IllegalArgumentException("Usage: <name> <email>");
        }

        Properties props = getConfigProps();
        LHConfig config = new LHConfig(props);
        LittleHorseBlockingStub client = config.getBlockingStub();

        String name = args[0];
        String email = args[1];

        System.out.println("Running workflow with name=%s email=%s".formatted(name, email));

        client.runWf(RunWfRequest.newBuilder()
                .setWfSpecName("assemble-person")
                .putVariables("name", VariableValue.newBuilder().setStr(name).build())
                .putVariables("email", VariableValue.newBuilder().setStr(email).build())
                .build());
    }
}
