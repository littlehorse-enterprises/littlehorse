package io.littlehorse.examples;

import io.grpc.StatusRuntimeException;
import io.grpc.Status.Code;
import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.IndexType;
import io.littlehorse.sdk.common.proto.PutExternalEventDefRequest;
import io.littlehorse.sdk.common.proto.VariableMutationType;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.LHPublicApiGrpc.LHPublicApiBlockingStub;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskWorker;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * This example demonstrates the asynchronous ExternalEvent functionality.
 * We will use "thread.waitForEvent" to wait for an external event, then when it arrives
 * it executes the task "greet".
 */
public class ExternalEventExample {

    private static final Logger log = LoggerFactory.getLogger(
        ExternalEventExample.class
    );

    public static Workflow getWorkflow() {
        return new WorkflowImpl(
            "example-external-event",
            thread -> {
                WfRunVariable name = thread.addVariable("name", VariableType.STR).withIndex(IndexType.REMOTE_INDEX);

                thread.execute("ask-for-name");

                thread.mutate(
                    name,
                    VariableMutationType.ASSIGN,
                    thread.waitForEvent("name-event")
                );

                thread.execute("greet", name);
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

    public static List<LHTaskWorker> getTaskWorkers(LHConfig config) throws IOException {
        WaitForExternalEventWorker executable = new WaitForExternalEventWorker();
        List<LHTaskWorker> workers = List.of(
            new LHTaskWorker(executable, "ask-for-name", config),
            new LHTaskWorker(executable, "greet", config)
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

    public static void main(String[] args) throws IOException {
        // Let's prepare the configurations
        Properties props = getConfigProps();
        LHConfig config = new LHConfig(props);
        LHPublicApiBlockingStub client = config.getBlockingStub();

        // New workflow
        Workflow workflow = getWorkflow();

        // New worker
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

        // Register external event if it does not exist
        Set<String> externalEventNames = workflow.getRequiredExternalEventDefNames();

        for (String externalEventName : externalEventNames) {
            log.debug("Registering external event {}", externalEventName);
            try {
                client.putExternalEventDef(
                    PutExternalEventDefRequest
                        .newBuilder()
                        .setName(externalEventName)
                        .build()
                );
            } catch(StatusRuntimeException exn) {
                if (exn.getStatus().getCode() == Code.ALREADY_EXISTS) {
                    log.debug("External event already exists!");
                } else {
                    throw exn;
                }
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
