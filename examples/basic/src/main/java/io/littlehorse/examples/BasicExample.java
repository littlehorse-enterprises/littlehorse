package io.littlehorse.examples;

import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.SaveUserTaskRunProgressRequest;
import io.littlehorse.sdk.common.proto.UserTaskRunId;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.sdk.common.proto.WfRunId;
import io.littlehorse.sdk.common.proto.SaveUserTaskRunProgressRequest.SaveUserTaskRunAssignmentPolicy;
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

    public static void main(String[] args) throws IOException {
        // Let's prepare the configurations
        Properties props = getConfigProps();
        LHConfig config = new LHConfig(props);

        SaveUserTaskRunProgressRequest req = SaveUserTaskRunProgressRequest.newBuilder()
                .setUserTaskRunId(UserTaskRunId.newBuilder().setWfRunId(WfRunId.newBuilder().setId(args[0])).setUserTaskGuid(args[1]))
                .setUserId("ahsoka")
                .putResults("requestedItem", LHLibUtil.objToVarVal("lightsaber"))
                .setPolicy(SaveUserTaskRunAssignmentPolicy.FAIL_IF_CLAIMED_BY_OTHER)
                .build();

        System.out.println(LHLibUtil.protoToJson(config.getBlockingStub().saveUserTaskRunProgress(req)));

        // // New workflow
        // Workflow workflow = getWorkflow();

        // // New worker
        // LHTaskWorker worker = getTaskWorker(config);

        // // Register task
        // worker.registerTaskDef();

        // // Register a workflow
        // workflow.registerWfSpec(config.getBlockingStub());

        // // Run the worker
        // worker.start();
    }
}
