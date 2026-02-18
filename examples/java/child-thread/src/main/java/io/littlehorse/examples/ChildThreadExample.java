package io.littlehorse.examples;

import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.Comparator;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * In this example you will see how to instantiate a child thread
 * and then wait until it has finished its execution before
 * executing another task
 */
public class ChildThreadExample {

    private static final Logger log = LoggerFactory.getLogger(ChildThreadExample.class);

    public static Workflow getWorkflow() {
        return new WorkflowImpl("example-child-thread", wf -> {
            WfRunVariable counter = wf.declareInt("counter").required();

            wf.doWhile(wf.condition(counter, Comparator.GREATER_THAN, 0), (whileBody) -> {
                SpawnedThread childThread = whileBody.spawnThread(
                        child -> {
                            child.complete();
                        },
                        "child-thread",
                        null);

                whileBody.waitForThreads(SpawnedThreads.of(childThread));

                counter.assign(counter.subtract(1));
            });
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
        ChildThreadWorker executable = new ChildThreadWorker();
        List<LHTaskWorker> workers = List.of(
                new LHTaskWorker(executable, "parent-task-1", config),
                new LHTaskWorker(executable, "child-task", config),
                new LHTaskWorker(executable, "parent-task-2", config));

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
        LHConfig config = new LHConfig(props);
        LittleHorseBlockingStub client = config.getBlockingStub();

        // New workflow
        Workflow workflow = getWorkflow();

        // New worker
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
