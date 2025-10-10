package io.littlehorse.examples;

import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.VariableMutationType;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.WfRunVariableAccessLevel;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskWorker;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;

public class ChildWorkflowExample {

    public static Workflow getChild() {
        return new WorkflowImpl(
            "some-other-wfspec",
            wf -> {
                // In the `hierarchical-workflow` example, we require the variable to be INHERITED;
                // however, here the variable is an input.
                WfRunVariable childInputName = wf.addVariable("child-input-name", VariableType.STR).required();
                wf.execute("greet", childInputName);
            }
        );
    }

    public static Workflow getParent() {
        WorkflowImpl out = new WorkflowImpl(
            "my-parent",
            wf -> {
                WfRunVariable theName = wf.addVariable("input-name", VariableType.STR).required();

                wf.runWf("some-other-wfspec", Map.of("child-input-name", theName));
            }
        );
        return out;
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

    public static void main(String[] args) throws IOException {
        // Let's prepare the configurations
        Properties props = getConfigProps();
        LHConfig config = new LHConfig(props);

        // New worker
        LHTaskWorker worker = getTaskWorker(config);
        worker.registerTaskDef();

        // Register the workflows.
        getChild().registerWfSpec(config.getBlockingStub());
        getParent().registerWfSpec(config.getBlockingStub());

        // Run the worker
        worker.start();
    }
}
