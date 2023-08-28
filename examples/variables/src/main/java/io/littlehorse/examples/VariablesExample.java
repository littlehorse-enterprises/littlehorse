package io.littlehorse.examples;

import static io.littlehorse.sdk.common.proto.IndexType.LOCAL_INDEX;
import static io.littlehorse.sdk.common.proto.IndexType.REMOTE_INDEX;

import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.LHPublicApiGrpc;
import io.littlehorse.sdk.common.proto.VariableMutationType;
import io.littlehorse.sdk.common.proto.VariableType;
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
            "example-variables",
            thread -> {
                WfRunVariable inputText = thread
                    .addVariable("input-text", VariableType.STR)
                    .withIndex(LOCAL_INDEX);
                WfRunVariable addLength = thread.addVariable(
                    "add-length",
                    VariableType.BOOL
                );
                WfRunVariable userId = thread
                    .addVariable("user-id", VariableType.INT)
                    .withIndex(REMOTE_INDEX);
                WfRunVariable sentimentScore = thread
                    .addVariable("sentiment-score", VariableType.DOUBLE)
                    .withIndex(LOCAL_INDEX);
                WfRunVariable processedResult = thread
                    .addVariable("processed-result", VariableType.JSON_OBJ)
                    .withJsonIndex("$.sentimentScore", REMOTE_INDEX);
                NodeOutput sentimentAnalysisOutput = thread.execute(
                    "sentiment-analysis",
                    inputText
                );
                thread.mutate(
                    sentimentScore,
                    VariableMutationType.ASSIGN,
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
                    VariableMutationType.ASSIGN,
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

    public static List<LHTaskWorker> getTaskWorker(LHConfig config) throws IOException {
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

    public static void main(String[] args) throws IOException {
        // Let's prepare the configurations
        Properties props = getConfigProps();
        LHConfig config = new LHConfig(props);
        LHPublicApiGrpc.LHPublicApiBlockingStub client = config.getBlockingStub();

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
