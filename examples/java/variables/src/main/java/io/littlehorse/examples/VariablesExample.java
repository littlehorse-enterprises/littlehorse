package io.littlehorse.examples;

import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc;
import io.littlehorse.sdk.common.proto.VariableMutationType;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.wfsdk.NodeOutput;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskWorker;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.Timestamp;
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
            wf -> {
                WfRunVariable inputText = wf.declareStr("input-text").searchable().masked();

                WfRunVariable addLength = wf.declareBool("add-length").searchable();

                WfRunVariable userId = wf.declareInt("user-id").searchable();

                WfRunVariable sentimentScore = wf.declareDouble("sentiment-score").searchable();

                WfRunVariable processedResult = wf
                    .declareJsonObj("processed-result")
                    .searchableOn("$.sentimentScore", VariableType.DOUBLE)
                    .masked();


                NodeOutput sentimentAnalysisOutput = wf.execute(
                    "sentiment-analysis",
                    inputText
                );
                sentimentScore.assign(sentimentAnalysisOutput);
                NodeOutput processedTextOutput = wf.execute(
                    "process-text",
                    inputText,
                    sentimentScore,
                    addLength,
                    userId
                );
                wf.mutate(
                    processedResult,
                    VariableMutationType.ASSIGN,
                    processedTextOutput
                );
                wf.execute("send", processedResult);

                NodeOutput currentDate = wf.execute("get-current-date");
//                NodeOutput oneHourLater = wf.execute("add-15-minutes", currentDate);
                wf.execute("print-date", currentDate);
                wf.execute("print-instant", currentDate);
                wf.execute("print-proto-timestamp", currentDate);
            }
        );
    }

    public static Properties getConfigProps() throws IOException {
        Properties props = new Properties();
        File configPath = Path.of(
            System.getProperty("user.home"),
            ".config/littlehorse.config"
        ).toFile();
        if(configPath.exists()){
            props.load(new FileInputStream(configPath));
        }
        return props;
    }

    public static List<LHTaskWorker> getTaskWorker(LHConfig config) {
        MyWorker executable = new MyWorker();
        List<LHTaskWorker> workers = List.of(
            new LHTaskWorker(executable, "sentiment-analysis", config),
            new LHTaskWorker(executable, "process-text", config),
            new LHTaskWorker(executable, "send", config),
            new LHTaskWorker(executable, "get-current-date", config),
            new LHTaskWorker(executable, "add-15-minutes", config),
            new LHTaskWorker(executable,  "print-proto-timestamp", config),
            new LHTaskWorker(executable,  "print-date", config),
            new LHTaskWorker(executable,  "print-instant", config)

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
        LittleHorseGrpc.LittleHorseBlockingStub client = config.getBlockingStub();

        // New workflow
        Workflow workflow = getWorkflow();

        // New worker
        List<LHTaskWorker> workers = getTaskWorker(config);

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
