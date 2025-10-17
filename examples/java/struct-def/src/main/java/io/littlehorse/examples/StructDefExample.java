package io.littlehorse.examples;

import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.RunWfRequest;
import io.littlehorse.sdk.common.proto.StructDefCompatibilityType;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.sdk.worker.LHTaskWorker;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StructDefExample {

    private static final Logger log = LoggerFactory.getLogger(StructDefExample.class);

    public static Workflow getWorkflow() {
        return new WorkflowImpl("example-struct-def", wf -> {
            WfRunVariable person = wf.declareStruct(
                "person",
                Person.class
            );

            wf.execute("greet", person.get("name"));
            wf.execute("describe-car", person.get("car"));
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

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            runWorkers();
        } else {
            runWf(args);
        }
    }

    public static void runWorkers() throws IOException {
        // Let's prepare the configurations
        Properties props = getConfigProps();
        LHConfig config = new LHConfig(props);

        Workflow workflow = getWorkflow();

        // New worker
        List<LHTaskWorker> workers = getTaskWorkers(config);

        workers.get(0).registerStructDef(Person.class, StructDefCompatibilityType.NO_SCHEMA_UPDATES);
        workers.get(0).registerStructDef(Car.class, StructDefCompatibilityType.NO_SCHEMA_UPDATES);

        // Register tasks if they don't exist
        for (LHTaskWorker worker : workers) {
            worker.registerTaskDef();
        }

        // Register WfSpec
        workflow.registerWfSpec(config.getBlockingStub());

        // Run the workers
        for (LHTaskWorker worker : workers) {
            log.debug("Starting {}", worker.getTaskDefName());
            worker.start();
        }
    }

    public static void runWf(String[] args) throws IOException {
        Properties props = getConfigProps();
        LHConfig config = new LHConfig(props);
        LittleHorseBlockingStub client = config.getBlockingStub();

        System.out.println("Running the workflow...");

        Person person = new Person("Obi-Wan Kenobi", new Car("Ford", "Escape"));

        client.runWf(RunWfRequest.newBuilder()
                .setWfSpecName("example-struct-def")
                .putVariables("person", LHLibUtil.objToVarVal(person))
                .build());
    }
}
