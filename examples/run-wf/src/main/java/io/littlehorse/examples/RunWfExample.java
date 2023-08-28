package io.littlehorse.examples;

import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.LHPublicApiGrpc;
import io.littlehorse.sdk.common.proto.RunWfRequest;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskWorker;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * In This example you are going to learn how to request a wf run
 * programmatically using:
 *
 * public String runWf(
 *       String wfSpecName,
 *       Integer wfSpecVersion,
 *       String wfRunId,
 *       Arg... args
 * )
 */
public class RunWfExample {

    private static final Logger log = LoggerFactory.getLogger(RunWfExample.class);

    public static Workflow getWorkflow() {
        return new WorkflowImpl(
            "example-run-wf",
            thread -> {
                WfRunVariable n = thread.addVariable("n", VariableType.INT);
                thread.execute("execution-number", n);
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
        MyWorker executable = new MyWorker();
        LHTaskWorker worker = new LHTaskWorker(
            executable,
            "execution-number",
            config
        );

        // Gracefully shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(worker::close));
        return worker;
    }

    public static void main(String[] args) throws IOException {
        // Let's prepare the configurations
        Properties props = getConfigProps();
        LHConfig config = new LHConfig(props);
        LHPublicApiGrpc.LHPublicApiBlockingStub client = config.getBlockingStub();

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

        // In another thread
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(
            new TimerTask() {
                int n = 1;

                @Override
                public void run() {
                    log.debug("Requesting wf run execution, n = {}", n);
                    try {
                        client.runWf(
                            RunWfRequest.newBuilder()
                                .setWfSpecName("example-run-wf")
                                .putVariables("n", LHLibUtil.objToVarVal(n))
                                .build()
                        );
                    } catch (Exception e) {
                        log.error("Error when calling the API", e);
                    }
                    n++;
                }
            },
            1000,
            1500
        );

        // Run the worker
        worker.start();
    }
}
