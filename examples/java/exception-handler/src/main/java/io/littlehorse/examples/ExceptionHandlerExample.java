package io.littlehorse.examples;

import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.wfsdk.NodeOutput;
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
 * Simple demonstration of a workflow that handles the failure of a task with
 * the handleException() functionality, which spawns a child thread and then
 * resumes execution when the handler thread completes.
 */
public class ExceptionHandlerExample {

    private static final Logger log = LoggerFactory.getLogger(ExceptionHandlerExample.class);

    public static Workflow getWorkflow() {
        return new WorkflowImpl("example-exception-handler", wf -> {
            NodeOutput node = wf.execute("fail");

            wf.handleError( // Handle technical failure
                    node, handler -> {
                        handler.execute("my-task");
                    });

            // Execution resumes after handling exception.
            wf.execute("my-task");
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
        ExceptionHandlerWorker executable = new ExceptionHandlerWorker();
        List<LHTaskWorker> workers =
                List.of(new LHTaskWorker(executable, "fail", config), new LHTaskWorker(executable, "my-task", config));

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
