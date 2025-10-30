package io.littlehorse.examples;

import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc;
import io.littlehorse.sdk.common.proto.VariableType;
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
        return new WorkflowImpl("example-json", wf -> {
            WfRunVariable person = wf.declareJsonObj("person");

            wf.execute("greet", person.jsonPath("$.name"));
            wf.execute("describe-car", person.jsonPath("$.car"));
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
        CarTaskWorker executable = new CarTaskWorker();
        List<LHTaskWorker> workers = List.of(
                new LHTaskWorker(executable, "greet", config), new LHTaskWorker(executable, "describe-car", config));

        // Gracefully shutdown
        Runtime.getRuntime()
                .addShutdownHook(new Thread(() -> workers.forEach(worker -> {
                    log.debug("Closing {}", worker.getTaskDefName());
                    worker.close();
                })));
        return workers;
    }

    public static void main(String[] args) throws IOException {
        // Let's prepare the configurations
        Properties props = getConfigProps();
        LHConfig config = new LHConfig();
        LittleHorseGrpc.LittleHorseBlockingStub client = config.getBlockingStub();

        // New workflow
        Workflow workflow = getWorkflow();

        // New workers
        List<LHTaskWorker> workers = getTaskWorkers(config);

        // Register tasks
        for (LHTaskWorker worker : workers) {
            worker.registerTaskDef();
        }

        // Register a workflow
        workflow.registerWfSpec(client);

        // Run the workers
        for (LHTaskWorker worker : workers) {
            log.debug("Starting {}", worker.getTaskDefName());
            worker.start();
        }
    }
}
