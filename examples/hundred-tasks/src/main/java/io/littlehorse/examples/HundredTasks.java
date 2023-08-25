package io.littlehorse.examples;

import io.littlehorse.sdk.common.config.LHWorkerConfig;
import io.littlehorse.sdk.common.proto.LHPublicApiGrpc.LHPublicApiBlockingStub;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskWorker;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * This is a simple example, which does two things:
 * 1. Declare an "input-name" variable of type String
 * 2. Pass that variable into the execution of the "greet" task.
 */
public class HundredTasks {

    private static final Logger log = LoggerFactory.getLogger(HundredTasks.class);

    public static Workflow getWorkflow() {
        return new WorkflowImpl(
            "hundred-tasks",
            thread -> {
                for (int i = 0; i < 25; i++) {
                    thread.execute("task-1");
                    thread.execute("task-2");
                    thread.execute("task-3");
                    thread.execute("task-4");
                }
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
        MyWorker executable = new MyWorker();
        List<LHTaskWorker> out = new ArrayList<>();

        out.add(new LHTaskWorker(executable, "task-1", config));
        out.add(new LHTaskWorker(executable, "task-2", config));
        out.add(new LHTaskWorker(executable, "task-3", config));
        out.add(new LHTaskWorker(executable, "task-4", config));

        // Gracefully shutdown
        for (LHTaskWorker w : out) {
            Runtime
                .getRuntime()
                .addShutdownHook(
                    new Thread(() -> {
                        w.close();
                    })
                );
        }
        return out;
    }

    public static void main(String[] args) throws IOException {
        // Let's prepare the configurations
        Properties props = getConfigProps();
        LHWorkerConfig config = new LHWorkerConfig(props);
        LHPublicApiBlockingStub client = config.getBlockingStub();

        // New workflow
        Workflow workflow = getWorkflow();

        // New worker
        List<LHTaskWorker> workers = getTaskWorkers(config);

        for (LHTaskWorker worker : workers) {
            // Register task if it does not exist
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

        // Run the worker
        for (LHTaskWorker worker : workers) {
            worker.start();
        }
    }
}
