package io.littlehorse.examples;

import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.VariableMutationType;
import io.littlehorse.sdk.common.proto.WfRunVariableAccessLevel;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskWorker;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;

public class ChildWorkflowExample {

    public static Workflow getGrandChild() {
        WorkflowImpl out = new WorkflowImpl("grand-child", wf -> {
            wf.waitForEvent("some-event").registeredAs(String.class);
        });
        out.setParent("child");

        return out;
    }

    public static Workflow getChild() {
        WorkflowImpl out = new WorkflowImpl("child", wf -> {
            WfRunVariable theName = wf.declareStr("name").withAccessLevel(WfRunVariableAccessLevel.INHERITED_VAR);

            wf.execute("greet", theName);

            wf.mutate(theName, VariableMutationType.ASSIGN, "yoda");
        });
        out.setParent("parent");

        return out;
    }

    public static Workflow getParent() {
        WorkflowImpl out = new WorkflowImpl("parent", wf -> {
            WfRunVariable theName = wf.declareStr("name").withAccessLevel(WfRunVariableAccessLevel.PUBLIC_VAR);

            wf.execute("greet", theName);
        });
        return out;
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

        // Register a workflow
        getParent().registerWfSpec(config.getBlockingStub());
        getChild().registerWfSpec(config.getBlockingStub());
        getGrandChild().registerWfSpec(config.getBlockingStub());

        // Run the worker
        worker.start();
    }
}
