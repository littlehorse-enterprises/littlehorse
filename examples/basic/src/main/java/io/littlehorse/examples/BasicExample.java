package io.littlehorse.examples;

import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.Comparator;
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
 * This is a simple example, which does two things:
 * 1. Declare an "input-name" variable of type String
 * 2. Pass that variable into the execution of the "greet" task.
 */
public class BasicExample {

    public static Workflow getWorkflow() {
        return new WorkflowImpl(
            "example-basic",
            wf -> {
                WfRunVariable theName = wf.addVariable("input-name", VariableType.STR).searchable();
                wf.execute("greet", theName);
                wf.waitForCondition(wf.condition(theName, Comparator.EQUALS, "test"));
                wf.execute("dismiss", theName);
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
        LHTaskWorker worker = new LHTaskWorker(executable, "greet", config);

        // Gracefully shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(worker::close));
        return worker;
    }

    public static LHTaskWorker getTaskWorker2(LHConfig config) {
        MyWorker2 executable = new MyWorker2();
        LHTaskWorker worker = new LHTaskWorker(executable, "dismiss", config);

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
        LHTaskWorker worker2 = getTaskWorker2(config);
        // Register task
        worker.registerTaskDef();
        worker2.registerTaskDef();

        // Register a workflow
        workflow.registerWfSpec(config.getBlockingStub());

        // Run the worker
        worker.start();
        worker2.start();
    }
}
