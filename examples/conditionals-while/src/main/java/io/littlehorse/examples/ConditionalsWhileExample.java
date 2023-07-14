package io.littlehorse.examples;

import io.littlehorse.sdk.client.LHClient;
import io.littlehorse.sdk.common.config.LHWorkerConfig;
import io.littlehorse.sdk.common.exception.LHApiError;
import io.littlehorse.sdk.common.proto.ComparatorPb;
import io.littlehorse.sdk.common.proto.VariableMutationTypePb;
import io.littlehorse.sdk.common.proto.VariableTypePb;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskWorker;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * In this example you will see how to define a while loop with LH.
 * Use doWhile(WorkflowCondition condition, ThreadFunc whileBody) to define the loop,
 * the end condition and the handler (body) of the while.
 */
public class ConditionalsWhileExample {

    private static final Logger log = LoggerFactory.getLogger(
        ConditionalsWhileExample.class
    );

    public static Workflow getWorkflow() {
        return new WorkflowImpl(
            "example-conditionals-while",
            thread -> {
                WfRunVariable numDonuts = thread.addVariable(
                    "number-of-donuts",
                    VariableTypePb.INT
                );

                thread.doWhile(
                    thread.condition(numDonuts, ComparatorPb.GREATER_THAN, 0),
                    handler -> {
                        handler.execute("eating-donut", numDonuts);
                        thread.mutate(numDonuts, VariableMutationTypePb.SUBTRACT, 1);
                    }
                );
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

    public static LHTaskWorker getTaskWorker(LHWorkerConfig config) {
        ConditionalWhileTaskWorker executable = new ConditionalWhileTaskWorker();
        LHTaskWorker worker = new LHTaskWorker(executable, "eating-donut", config);

        // Gracefully shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(worker::close));
        return worker;
    }

    public static void main(String[] args) throws IOException, LHApiError {
        // Let's prepare the configurations
        Properties props = getConfigProps();
        LHWorkerConfig config = new LHWorkerConfig(props);
        LHClient client = new LHClient(config);

        // New workflow
        Workflow workflow = getWorkflow();

        // New worker
        LHTaskWorker worker = getTaskWorker(config);

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
        worker.start();
    }
}
