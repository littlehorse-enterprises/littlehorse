package io.littlehorse.examples;

import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.wfsdk.SpawnedThreads;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.WorkflowThread;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskWorker;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;

import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.wfsdk.SpawnedThreads;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.WorkflowThread;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskWorker;

/*
 * In this example you will see how to spawn multiples threads base on a INPUT json array.
 */
public class SpawnThreadForEachExample {

    public static Workflow getWorkflow() {
        return new WorkflowImpl("spawn-parallel-threads-from-json-arr-variable", wf -> {
            WfRunVariable approvalChain = wf.declareJsonObj("approval-chain");
            SpawnedThreads spawnedThreads = wf.spawnThreadForEach(
                    approvalChain.jsonPath("$.approvals"),
                    "spawn-threads",
                    innerThread -> {
                        // It is mandatory to use ThreadBuilder.HANDLER_INPUT_VAR at the moment.
                        innerThread.declareInt("not-used-variable");
                        WfRunVariable inputVariable = innerThread.declareJsonObj(WorkflowThread.HANDLER_INPUT_VAR);
                        innerThread.execute("task-executor", inputVariable.jsonPath("$.user"));
                    },
                    Map.of("not-used-variable", 1234));
            wf.waitForThreads(spawnedThreads);

            wf.execute("task-executor", approvalChain.jsonPath("$.description"));
        });
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
        SpawnThreadForEachWorker executable = new SpawnThreadForEachWorker();
        LHTaskWorker worker = new LHTaskWorker(executable, "task-executor", config);

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
