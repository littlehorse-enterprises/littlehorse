package io.littlehorse.examples;

import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/*
 * This is a simple example, which does
 * 1. Declare an "input-name" variable of type String
 * 2. Pass that variable into the execution of the "greet" task.
 * 3. Shows the cluster's health status.
 */
public class HealthStatus {


    private static final Logger log = LoggerFactory.getLogger(HealthStatus.class);

    public static Workflow getWorkflow() {
        return new WorkflowImpl(
            "example-health-status",
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
        Runtime.getRuntime().addShutdownHook(new Thread(worker::close));

        ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutor.scheduleAtFixedRate(() -> log.debug("Worker status: {}", worker.healthStatus().getReason()), 1, 1, TimeUnit.SECONDS);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            scheduledExecutor.shutdownNow();
            try {
                scheduledExecutor.awaitTermination(1, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                // ignore
            }
        }));

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
