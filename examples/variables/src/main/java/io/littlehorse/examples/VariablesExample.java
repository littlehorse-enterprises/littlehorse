package io.littlehorse.examples;

import io.littlehorse.sdk.client.LHClient;
import io.littlehorse.sdk.common.config.LHWorkerConfig;
import io.littlehorse.sdk.common.exception.LHApiError;
import io.littlehorse.sdk.common.proto.IndexTypePb;
import io.littlehorse.sdk.common.proto.VariableMutationTypePb;
import io.littlehorse.sdk.common.proto.VariableTypePb;
import io.littlehorse.sdk.wfsdk.NodeOutput;
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
 * This is a simple example, which does two things:
 * 1. Declare an "input-name" variable of type String
 * 2. Pass that variable into the execution of the "greet" task.
 */
public class VariablesExample {

    private static final Logger log = LoggerFactory.getLogger(VariablesExample.class);

    public static Workflow getWorkflow() {
        return new WorkflowImpl(
            "example-variables3",
            thread -> {
                WfRunVariable inputText = thread
                    .addVariable("input-text", VariableTypePb.STR)
                    .withIndex(IndexTypePb.REMOTE_INDEX);
                WfRunVariable addLength = thread
                    .addVariable("add-length", VariableTypePb.BOOL)
                    .withIndex(IndexTypePb.LOCAL_INDEX);
                WfRunVariable userId = thread
                    .addVariable("user-id", VariableTypePb.INT)
                    .withIndex(IndexTypePb.LOCAL_INDEX);
                WfRunVariable sentimentScore = thread
                    .addVariable("sentiment-score", VariableTypePb.DOUBLE)
                    .withIndex(IndexTypePb.REMOTE_INDEX);
                WfRunVariable processedResult = thread
                    .addVariable("processed-result", VariableTypePb.JSON_OBJ)
                    .withJsonIndex("$.text", IndexTypePb.LOCAL_INDEX)
                    .withJsonIndex("$.userId", IndexTypePb.REMOTE_INDEX);
                NodeOutput sentimentAnalysisOutput = thread.execute(
                    "sentiment-analysis",
                    inputText
                );
                thread.mutate(
                    sentimentScore,
                    VariableMutationTypePb.ASSIGN,
                    sentimentAnalysisOutput
                );
                NodeOutput processedTextOutput = thread.execute(
                    "process-text",
                    inputText,
                    sentimentScore,
                    addLength,
                    userId
                );
                thread.mutate(
                    processedResult,
                    VariableMutationTypePb.ASSIGN,
                    processedTextOutput
                );
                thread.execute("send", processedResult);
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

    public static List<LHTaskWorker> getTaskWorker(LHWorkerConfig config) {
        MyWorker executable = new MyWorker();
        List<LHTaskWorker> workers = List.of(
            new LHTaskWorker(executable, "sentiment-analysis", config),
            new LHTaskWorker(executable, "process-text", config),
            new LHTaskWorker(executable, "send", config)
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

        // New worker
        List<LHTaskWorker> workers = getTaskWorker(config);

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
