package io.littlehorse.examples;

import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskWorker;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/*
 * This is a simple example, which does two things:
 * 1. Declare an "input-name" variable of type String
 * 2. Pass that variable into the execution of the "greet" task.
 */
public class HundredTasks {

    public static Workflow getWorkflow() {
        return new WorkflowImpl(
            "hundred-tasks",
            wf -> {
                for (int i = 0; i < 25; i++) {
                    wf.execute("task-1");
                    wf.execute("task-2");
                    wf.execute("task-3");
                    wf.execute("task-4");
                }
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

    public static List<LHTaskWorker> getTaskWorkers(LHConfig config) {
        MyWorker executable = new MyWorker();
        List<LHTaskWorker> out = new ArrayList<>();

        out.add(new LHTaskWorker(executable, "task-1", config));
        out.add(new LHTaskWorker(executable, "task-2", config));
        out.add(new LHTaskWorker(executable, "task-3", config));
        out.add(new LHTaskWorker(executable, "task-4", config));

        // Gracefully shutdown
        for (LHTaskWorker w : out) {
            Runtime
                .getRuntime()
                .addShutdownHook(
                    new Thread(() -> {
                        w.close();
                    })
                );
        }
        return out;
    }

    public static void main(String[] args) throws IOException {
        // Let's prepare the configurations
        Properties props = getConfigProps();
        LHConfig config = new LHConfig(props);
        LittleHorseBlockingStub client = config.getBlockingStub();

        // New workflow
        Workflow workflow = getWorkflow();

        // New worker
        List<LHTaskWorker> workers = getTaskWorkers(config);

        for (LHTaskWorker worker : workers) {
            // Register task
            worker.registerTaskDef();
        }

        // Register a workflow
        workflow.registerWfSpec(client);

        // Run the worker
        for (LHTaskWorker worker : workers) {
            worker.start();
        }
    }
}
