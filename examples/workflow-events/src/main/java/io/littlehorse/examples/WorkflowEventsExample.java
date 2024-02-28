package io.littlehorse.examples;

import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.Comparator;
import io.littlehorse.sdk.common.proto.VariableMutationType;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.wfsdk.TaskNodeOutput;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskWorker;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;

public class WorkflowEventsExample {

    public static Workflow getWorkflow() {
        return new WorkflowImpl(
            "example-workflow-events",
            wf -> {
                WfRunVariable numberOfIterations = wf.addVariable("number-of-iterations", VariableType.DOUBLE);
                WfRunVariable currentValue = wf.addVariable("current-value", 0.0);
                wf.throwEvent("BEGIN", currentValue);
                WfRunVariable halfEventThrown = wf.addVariable("half-event-thrown", VariableType.BOOL);
                wf.addVariable("end-event-thrown", VariableType.STR);
                WfRunVariable progress = wf.addVariable("progress", VariableType.DOUBLE);
                wf.doWhile(wf.condition(currentValue, Comparator.LESS_THAN_EQ, numberOfIterations), whileBody -> {
                    TaskNodeOutput newValue = whileBody.execute("increment", currentValue);
                    whileBody.mutate(currentValue, VariableMutationType.ASSIGN, newValue);
                    whileBody.mutate(progress, VariableMutationType.ASSIGN, currentValue);
                    whileBody.mutate(progress, VariableMutationType.DIVIDE, numberOfIterations);
                    whileBody.mutate(progress, VariableMutationType.MULTIPLY, 100.0);
                    whileBody.doIf(whileBody.condition(progress, Comparator.GREATER_THAN_EQ, 50.0), ifBody -> {
                        ifBody.doIf(ifBody.condition(halfEventThrown, Comparator.EQUALS, false), thread -> {
                            thread.throwEvent("half-event", currentValue);
                            thread.mutate(halfEventThrown, VariableMutationType.ASSIGN, true);
                        });
                    });
                });
                wf.throwEvent("end-event", currentValue);
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
        MyWorker executable = new MyWorker();
        LHTaskWorker worker = new LHTaskWorker(executable, "increment", config);

        // Gracefully shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(worker::close));
        return worker;
    }

    public static void main(String[] args) throws IOException {
        // Let's prepare the configurations
        Properties props = getConfigProps();
        LHConfig config = new LHConfig(props);

        // New workflow
        Workflow workflow = getWorkflow();

        // New worker
        LHTaskWorker worker = getTaskWorker(config);

        // Register task
        worker.registerTaskDef();

        // Register a workflow
        workflow.registerWfSpec(config.getBlockingStub());

        // Run the worker
        worker.start();
    }
}
