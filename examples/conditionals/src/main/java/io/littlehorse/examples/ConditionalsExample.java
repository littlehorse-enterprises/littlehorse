package io.littlehorse.examples;

import io.littlehorse.sdk.client.LHClient;
import io.littlehorse.sdk.common.config.LHWorkerConfig;
import io.littlehorse.sdk.common.exception.LHApiError;
import io.littlehorse.sdk.common.proto.ComparatorPb;
import io.littlehorse.sdk.common.proto.VariableTypePb;
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
 * In this example you will see how to use conditionals.
 * It will execute an "if" or "else" depending on the value of "bar".
 * If bar is greater than 10 then execute task-b else execute task-c.
 */
public class ConditionalsExample {

    private static final Logger log = LoggerFactory.getLogger(
        ConditionalsExample.class
    );

    public static Workflow getWorkflow() {
        return new WorkflowImpl(
            "example-conditionals",
            thread -> {
                WfRunVariable foo = thread.addVariable(
                    "foo",
                    VariableTypePb.JSON_OBJ
                );

                thread.execute("task-a");

                thread.doIfElse(
                    thread.condition(
                        foo.jsonPath("$.bar"),
                        ComparatorPb.GREATER_THAN,
                        10
                    ),
                    ifHandler -> {
                        ifHandler.execute("task-b");
                    },
                    elseHandler -> {
                        elseHandler.execute("task-c");
                    }
                );

                thread.execute("task-d");
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

    public static List<LHTaskWorker> getTaskWorkers(LHWorkerConfig config) {
        ConditionalsTaskWorker executable = new ConditionalsTaskWorker();
        List<LHTaskWorker> workers = List.of(
            new LHTaskWorker(executable, "task-a", config),
            new LHTaskWorker(executable, "task-b", config),
            new LHTaskWorker(executable, "task-c", config),
            new LHTaskWorker(executable, "task-d", config)
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

    public static void main(String[] args) throws IOException, LHApiError {
        // Let's prepare the configurations
        Properties props = getConfigProps();
        LHWorkerConfig config = new LHWorkerConfig(props);
        LHClient client = new LHClient(config);

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
