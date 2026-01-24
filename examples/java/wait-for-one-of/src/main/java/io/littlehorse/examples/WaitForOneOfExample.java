package io.littlehorse.examples;

import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.PutExternalEventDefRequest;
import io.littlehorse.sdk.wfsdk.SpawnedThread;
import io.littlehorse.sdk.wfsdk.SpawnedThreads;
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
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * In this example you will see how to instantiate a child thread
 * and then wait until it has finished its execution before
 * executing another task
 */
public class WaitForOneOfExample {

    private static final Logger log = LoggerFactory.getLogger(WaitForOneOfExample.class);

    public static Workflow getWorkflow() {
        return new WorkflowImpl("example-wait-for-one-of", wf -> {
            // Spawn two child threads that wait for different external events
            SpawnedThread childThread1 = wf.spawnThread(
                    child -> {
                        child.waitForEvent("child-1-event");
                    },
                    "child-1",
                    Map.of());

            SpawnedThread childThread2 = wf.spawnThread(
                    child -> {
                        child.waitForEvent("child-2-event");
                    },
                    "child-2",
                    Map.of());

            // Wait for any one of the child threads to complete
            wf.waitForAnyOf(SpawnedThreads.of(childThread1, childThread2));

            // Execute a task after one child completes
            wf.execute("child-completed");
        });
    }

    public static List<LHTaskWorker> getTaskWorkers(LHConfig config) {
        WaitForOneOfWorker executable = new WaitForOneOfWorker();
        List<LHTaskWorker> workers = List.of(new LHTaskWorker(executable, "child-completed", config));

        // Gracefully shutdown
        Runtime.getRuntime()
                .addShutdownHook(new Thread(() -> workers.forEach(worker -> {
                    log.debug("Closing {}", worker.getTaskDefName());
                    worker.close();
                })));
        return workers;
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
        // Let's prepare the configurations
        Properties props = getConfigProps();
        LHConfig config = new LHConfig(props);
        LittleHorseBlockingStub client = config.getBlockingStub();

        // New workflow
        Workflow workflow = getWorkflow();

        // New workers
        List<LHTaskWorker> workers = getTaskWorkers(config);

        // Register tasks if they don't exist
        for (LHTaskWorker worker : workers) {
            worker.registerTaskDef();
        }

        // Register external events if they do not exist
        Set<String> externalEventNames = workflow.getRequiredExternalEventDefNames();

        for (String externalEventName : externalEventNames) {
            log.debug("Registering external event {}", externalEventName);
            client.putExternalEventDef(PutExternalEventDefRequest.newBuilder()
                    .setName(externalEventName)
                    .build());
        }

        // Register a workflow if it does not exist
        workflow.registerWfSpec(client);

        // Run the workers
        for (LHTaskWorker worker : workers) {
            log.debug("Starting {}", worker.getTaskDefName());
            worker.start();
        }
    }
}
