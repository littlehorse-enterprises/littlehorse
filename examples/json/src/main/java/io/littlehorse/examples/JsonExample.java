package io.littlehorse.examples;

<<<<<<< Updated upstream
import io.littlehorse.sdk.common.config.LHWorkerConfig;
import io.littlehorse.sdk.common.proto.LHPublicApiGrpc;
=======
import io.littlehorse.sdk.common.proto.LHPublicApiGrpc.LHPublicApiBlockingStub;
import io.littlehorse.sdk.common.config.LHWorkerConfig;
import java.io.IOException;
>>>>>>> Stashed changes
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskWorker;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * This workflow demonstrates the ability of the Java SDK to serialize/deserialize
 * JSON and allow Task developers to interact with real Java objects.
 * More information about json-path at https://github.com/json-path/JsonPath.
 *
 * To run this workflow:
 * `lhctl run json-example person '{"name": "Obi-Wan", "car": {"brand": "Ford", "model": "Escape"}}'`
 */
public class JsonExample {

    private static final Logger log = LoggerFactory.getLogger(JsonExample.class);

    public static Workflow getWorkflow() {
        return new WorkflowImpl(
            "example-json",
            thread -> {
                WfRunVariable person = thread.addVariable(
                    "person",
                    VariableType.JSON_OBJ
                );

                thread.execute("greet", person.jsonPath("$.name"));
                thread.execute("describe-car", person.jsonPath("$.car"));
            }
        );
    }

    public static Properties getConfigProps() throws IOException {
        Properties props = new Properties();
        Path configPath = Path.of(
            System.getProperty("user.home"),
            ".config/littlehorse.config"
        );
        props.load(new FileInputStream(configPath.toFile()));
        return props;
    }

    public static List<LHTaskWorker> getTaskWorkers(LHWorkerConfig config) throws IOException {
        CarTaskWorker executable = new CarTaskWorker();
        List<LHTaskWorker> workers = List.of(
            new LHTaskWorker(executable, "greet", config),
            new LHTaskWorker(executable, "describe-car", config)
        );

        // Gracefully shutdown
        Runtime
            .getRuntime()
            .addShutdownHook(
                new Thread(() ->
                    workers.forEach(worker -> {
                        log.debug("Closing {}", worker.getTaskDefName());
                        worker.close();
                    })
                )
            );
        return workers;
    }

<<<<<<< Updated upstream
    public static void main(String[] args) throws IOException {
=======
    public static void main(String[] args) throws IOException, IOException {
>>>>>>> Stashed changes
        // Let's prepare the configurations
        Properties props = getConfigProps();
        LHWorkerConfig config = new LHWorkerConfig(props);
        LHPublicApiGrpc.LHPublicApiBlockingStub client = config.getBlockingStub();

        // New workflow
        Workflow workflow = getWorkflow();

        // New workers
        List<LHTaskWorker> workers = getTaskWorkers(config);

        // Register tasks if they don't exist
        for (LHTaskWorker worker : workers) {
            if (worker.doesTaskDefExist()) {
                log.debug(
                    "Task {} already exists, skipping creation",
                    worker.getTaskDefName()
                );
            } else {
                log.debug(
                    "Task {} does not exist, registering it",
                    worker.getTaskDefName()
                );
                worker.registerTaskDef();
            }
        }

        // Register a workflow if it does not exist
        if (workflow.doesWfSpecExist(client)) {
            log.debug(
                "Workflow {} already exists, skipping creation",
                workflow.getName()
            );
        } else {
            log.debug(
                "Workflow {} does not exist, registering it",
                workflow.getName()
            );
            workflow.registerWfSpec(client);
        }

        // Run the workers
        for (LHTaskWorker worker : workers) {
            log.debug("Starting {}", worker.getTaskDefName());
            worker.start();
        }
    }
}
