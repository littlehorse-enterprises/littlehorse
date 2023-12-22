package io.littlehorse.examples;

import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.wfsdk.SpawnedThreads;
import io.littlehorse.sdk.wfsdk.WorkflowThread;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskWorker;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * In this example you will see how to spawn multiples threads base on a INPUT json array.
 */
public class SpawnThreadForEachExample {

    private static final Logger log = LoggerFactory.getLogger(
        SpawnThreadForEachExample.class
    );

    public static Workflow getWorkflow() {
        return new WorkflowImpl(
            "spawn-parallel-threads-from-json-arr-variable",
            wf -> {
                WfRunVariable approvalChain = wf.addVariable(
                    "approval-chain",
                    VariableType.JSON_OBJ
                );
                SpawnedThreads spawnedThreads = wf.spawnThreadForEach(approvalChain.jsonPath("$.approvals"), "spawn-threads", innerThread -> {
                    // It is mandatory to use ThreadBuilder.HANDLER_INPUT_VAR at the moment.
                    innerThread.addVariable("not-used-variable", VariableType.INT);
                    WfRunVariable inputVariable = innerThread.addVariable(WorkflowThread.HANDLER_INPUT_VAR, VariableType.JSON_OBJ);
                    innerThread.execute("task-executor", inputVariable.jsonPath("$.user"));
                },
                Map.of("not-used-variable", 1234)
                );
                wf.waitForThreads(spawnedThreads);

                wf.execute("task-executor", approvalChain.jsonPath("$.description"));
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

    public static LHTaskWorker getTaskWorker(LHConfig config) throws IOException {
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
