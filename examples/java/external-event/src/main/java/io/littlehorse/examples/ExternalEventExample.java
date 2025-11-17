package io.littlehorse.examples;

import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.PutExternalEventDefRequest;
import io.littlehorse.sdk.common.proto.VariableMutationType;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.WorkflowRetentionPolicy;
import io.littlehorse.sdk.wfsdk.SpawnedThread;
import io.littlehorse.sdk.wfsdk.SpawnedThreads;
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
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * This example demonstrates the asynchronous ExternalEvent functionality. We will use
 * "thread.waitForEvent" to wait for an external event, then when it arrives it executes the task
 * "greet".
 */
public class ExternalEventExample {

    private static final Logger log = LoggerFactory.getLogger(ExternalEventExample.class);

    public static Workflow getWorkflow() {
        return new WorkflowImpl("example-external-event", wf -> {
            WfRunVariable name = wf.declareStr("name").searchable();

           wf.execute("ask-for-name");

            wf.mutate(name, VariableMutationType.ASSIGN, wf.waitForEvent("name-event"));
            var output =  wf.execute("greet", name);
            wf.waitForEvent("name-event");
            wf.execute("greet", output);
            SpawnedThread childThread = wf.spawnThread(child -> { // this is the child workflow
                                                                  // thread
                WfRunVariable childVar = child.addVariable("child-var", VariableType.STR);
                child.execute("greet", childVar);
            }, "spawned-thread", Map.of("child-var", name));
            wf.waitForThreads(SpawnedThreads.of(childThread));

            wf.waitForEvent("name-event");
            wf.execute("greet", output);
        });
    }

    public static Properties getConfigProps() throws IOException {
        Properties props = new Properties();
        File configPath =
                Path.of(System.getProperty("user.home"), ".config/littlehorse.config").toFile();
        if (configPath.exists()) {
            props.load(new FileInputStream(configPath));
        }
        return props;
    }

    public static List<LHTaskWorker> getTaskWorkers(LHConfig config) {
        WaitForExternalEventWorker executable = new WaitForExternalEventWorker();
        List<LHTaskWorker> workers = List.of(new LHTaskWorker(executable, "ask-for-name", config),
                new LHTaskWorker(executable, "greet", config));

        // Gracefully shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> workers.forEach(worker -> {
            log.debug("Closing {}", worker.getTaskDefName());
            worker.close();
        })));
        return workers;
    }

    public static void main(String[] args) throws IOException {
        // Let's prepare the configurations
        Properties props = getConfigProps();
        LHConfig config = new LHConfig(props);
        LittleHorseBlockingStub client = config.getBlockingStub();

        // New workflow
        Workflow workflow = getWorkflow().withRetentionPolicy(
                WorkflowRetentionPolicy.newBuilder().setSecondsAfterWfTermination(10).build());

        // New worker
        List<LHTaskWorker> workers = getTaskWorkers(config);

        // Register tasks if they don't exist
        for (LHTaskWorker worker : workers) {
            worker.registerTaskDef();
        }

        // Register external event if it does not exist
        Set<String> externalEventNames = workflow.getRequiredExternalEventDefNames();

        for (String externalEventName : externalEventNames) {
            log.debug("Registering external event {}", externalEventName);
            client.putExternalEventDef(
                    PutExternalEventDefRequest.newBuilder().setName(externalEventName).build());
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
