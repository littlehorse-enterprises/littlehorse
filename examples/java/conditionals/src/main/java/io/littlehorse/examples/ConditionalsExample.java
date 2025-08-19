package io.littlehorse.examples;

import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.Comparator;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
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
            wf -> {
                WfRunVariable foo = wf.addVariable("foo", VariableType.JSON_OBJ);

                wf.execute("task-a");

                wf.doIf(
                    wf.condition(
                        foo.jsonPath("$.bar"),
                        Comparator.GREATER_THAN,
                        10
                    ),
                    ifHandler -> {
                        ifHandler.execute("task-b");
                    }
                ).doElse(
                    elseHandler -> {
                        elseHandler.execute("task-c");
                    }
                );

                wf.execute("task-d");
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

    public static List<LHTaskWorker> getTaskWorkers(LHConfig config) {
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

    public static void main(String[] args) throws IOException {
        // Let's prepare the configurations
        Properties props = getConfigProps();
        LHConfig config = new LHConfig(props);
        LittleHorseBlockingStub client = config.getBlockingStub();

        // New workflow
        Workflow workflow = getWorkflow();

        // New workers
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
