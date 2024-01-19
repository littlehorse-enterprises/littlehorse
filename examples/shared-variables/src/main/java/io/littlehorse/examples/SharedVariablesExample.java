package io.littlehorse.examples;

import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.RunWfRequest;
import io.littlehorse.sdk.common.proto.VariableMutationType;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.WfRunId;
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
/*
 * This is a simple example, which does two things:
 * 1. Declare an "input-name" variable of type String
 * 2. Pass that variable into the execution of the "greet" task.
 */
public class SharedVariablesExample {

    public static Workflow getParentWorkflow() {
        WorkflowImpl out = new WorkflowImpl(
                "parent-wf",
                wf -> {
                    WfRunVariable theName = wf.addVariable(
                            "input-name",
                            VariableType.STR
                    ).withAccessLevel(WfRunVariableAccessLevel.PUBLIC_VAR);

                    wf.execute("greet", theName);

                    wf.mutate(theName, VariableMutationType.ASSIGN, "yoda");
                }
        );
        return out;
    }
    public static Workflow getChildWorkflow() {
        WorkflowImpl out = new WorkflowImpl(
            "child-wf",
            wf -> {
                WfRunVariable theName = wf.addVariable(
                    "input-name",
                    VariableType.STR
                ).withAccessLevel(WfRunVariableAccessLevel.INHERITED_VAR);

                wf.execute("greet", theName);

                wf.mutate(theName, VariableMutationType.ASSIGN, "yoda");
            }
        );
        out.setParent("parent-wf");

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

    public static LHTaskWorker getTaskWorker(LHConfig config) throws IOException {
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

        Workflow parentWorkflow = getParentWorkflow();
        Workflow childWorkflow = getChildWorkflow();

        // New worker
        LHTaskWorker worker = getTaskWorker(config);
        worker.registerTaskDef();
        parentWorkflow.registerWfSpec(config.getBlockingStub());
        childWorkflow.registerWfSpec(config.getBlockingStub());
        worker.start();
    }
}
