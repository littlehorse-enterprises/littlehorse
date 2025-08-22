package io.littlehorse.examples;

import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.Comparator;
import io.littlehorse.sdk.common.proto.VariableMutationType;
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
import java.util.Properties;

/*
 * In this example you will see how to define a while loop with LH.
 * Use doWhile(WorkflowCondition condition, ThreadFunc whileBody) to define the loop,
 * the end condition and the handler (body) of the while.
 */
public class ConditionalsWhileExample {

    public static Workflow getWorkflow() {
        return new WorkflowImpl(
            "example-conditionals-while",
            wf -> {
                WfRunVariable numDonuts = wf.addVariable(
                    "number-of-donuts",
                    VariableType.INT
                ).required();

                wf.doWhile(
                        wf.condition(numDonuts, Comparator.GREATER_THAN, 0),
                    handler -> {
                        handler.mutate(numDonuts, VariableMutationType.SUBTRACT, 1);
                        handler.execute("eating-donut", numDonuts);
                    }
                );
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

    public static LHTaskWorker getTaskWorker(LHConfig config) {
        ConditionalWhileTaskWorker executable = new ConditionalWhileTaskWorker();
        LHTaskWorker worker = new LHTaskWorker(executable, "eating-donut", config);

        // Gracefully shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(worker::close));
        return worker;
    }

    public static void main(String[] args) throws IOException {
        // Let's prepare the configurations
        Properties props = getConfigProps();
        LHConfig config = new LHConfig(props);
        LittleHorseBlockingStub client = config.getBlockingStub();

        // New workflow
        Workflow workflow = getWorkflow();

        // New worker
        LHTaskWorker worker = getTaskWorker(config);

        // Register task
        worker.registerTaskDef();

        // Register a workflow
        workflow.registerWfSpec(client);

        // Run the worker
        worker.start();
    }
}
