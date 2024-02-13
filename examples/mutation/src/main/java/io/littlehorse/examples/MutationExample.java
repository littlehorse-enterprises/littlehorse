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
import java.util.Properties;

/*
 * In this example you will see how to mutate variables.
 */
public class MutationExample {

    public static Workflow getWorkflow() {
        return new WorkflowImpl(
            "example-mutation",
            wf -> {
                WfRunVariable theName = wf.addVariable("name", VariableType.STR);
                // We pass the name of the person and receive if it is spider-man or not
                NodeOutput output = wf.execute("spider-bite", theName);

                // We save the output in the variable
                wf.mutate(theName, VariableMutationType.ASSIGN, output);
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
        SpiderManMutator executable = new SpiderManMutator();
        LHTaskWorker worker = new LHTaskWorker(executable, "spider-bite", config);

        // Gracefully shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(worker::close));
        return worker;
    }

    public static void main(String[] args) throws IOException {
        // Let's prepare the configurations
        Properties props = getConfigProps();
        LHConfig config = new LHConfig(props);
        LittleHorseGrpc.LittleHorseBlockingStub client = config.getBlockingStub();

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
