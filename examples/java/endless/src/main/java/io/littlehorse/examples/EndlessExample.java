package io.littlehorse.examples;

import io.littlehorse.sdk.common.config.LHConfig;
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
 * WfRuns loop forever (heartbeat task, then sleep) unless "max-iterations" is set.
 */
public class EndlessExample {

    public static Workflow getWorkflow() {
        return new WorkflowImpl("example-endless", wf -> {
            WfRunVariable intervalSeconds = wf.declareInt("interval-seconds").withDefault(60);
            WfRunVariable maxIterations = wf.declareInt("max-iterations").withDefault(-1);
            WfRunVariable iteration = wf.declareInt("iteration").withDefault(0);

            wf.doWhile(iteration.isNotEqualTo(maxIterations), loop -> {
                loop.execute("endless-heartbeat", iteration);
                iteration.assign(iteration.add(1));
                loop.sleepSeconds(intervalSeconds);
            });
        });
    }

    public static Properties getConfigProps() throws IOException {
        Properties props = new Properties();
        File configPath = Path.of(System.getProperty("user.home"), ".config/littlehorse.config")
                .toFile();
        if (configPath.exists()) {
            props.load(new FileInputStream(configPath));
        }
        // LHC_* env vars override the config file.
        System.getenv().forEach((key, value) -> {
            if (key.startsWith("LHC_")) props.put(key, value);
        });
        return props;
    }

    public static LHTaskWorker getTaskWorker(LHConfig config) {
        LHTaskWorker worker = new LHTaskWorker(new HeartbeatWorker(), "endless-heartbeat", config);
        Runtime.getRuntime().addShutdownHook(new Thread(worker::close));
        return worker;
    }

    public static void main(String[] args) throws IOException {
        LHConfig config = new LHConfig(getConfigProps());
        LHTaskWorker worker = getTaskWorker(config);

        worker.registerTaskDef();
        getWorkflow().registerWfSpec(config.getBlockingStub());

        worker.start();
    }
}
