package io.littlehorse.examples;

import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc;
import io.littlehorse.sdk.wfsdk.NodeOutput;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskWorker;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Timestamp example.
 *
 * This workflow demonstrates declaring a timestamp variable and using it across tasks.
 * It performs the following:
 * 1. Declares a timestamp variable `publish-date` (with a default Instant).
 * 2. Declares a string variable `book-name` (with a default value).
 * 3. Executes `publish-book` (creates a Book with multiple timestamp representations),
 *    `get-current-date` (returns the current date), and `print-book-details` (logs details).
 */
public class TimestampExample {

    private static final Logger log = LoggerFactory.getLogger(TimestampExample.class);

    public static Workflow getWorkflow() {
        return new WorkflowImpl("example-timestamp", wf -> {
            WfRunVariable publishDate =
                    wf.declareTimestamp("publish-date").withDefault(Instant.parse("1997-06-26T12:12:12Z"));
            WfRunVariable bookName = wf.declareStr("book-name").withDefault("Harry Potter and the Philosopher's Stone");
            NodeOutput publishBook = wf.execute("publish-book", bookName, publishDate);
            NodeOutput currentDate = wf.execute("get-current-date");
            wf.execute("print-book-details", publishBook, currentDate);
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
        Worker executable = new Worker();
        List<LHTaskWorker> workers = List.of(
                new LHTaskWorker(executable, "get-current-date", config),
                new LHTaskWorker(executable, "publish-book", config),
                new LHTaskWorker(executable, "print-book-details", config));
        Runtime.getRuntime()
                .addShutdownHook(new Thread(() -> workers.forEach(worker -> {
                    log.debug("Closing {}", worker.getTaskDefName());
                    worker.close();
                })));
        return workers;
    }

    public static void main(String[] args) throws IOException {
        Properties props = getConfigProps();
        LHConfig config = new LHConfig(props);
        LittleHorseGrpc.LittleHorseBlockingStub client = config.getBlockingStub();

        Workflow workflow = getWorkflow();

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
