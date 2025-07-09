package io.littlehorse.examples;

import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.StructDefCompatibilityType;
import io.littlehorse.sdk.worker.LHTaskWorker;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;

public class StructDefExample {

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

        // Register StructDefs
        worker.registerStructDefs(StructDefCompatibilityType.NO_SCHEMA_UPDATES);

        // Register task
        worker.registerTaskDef();

        // Run the worker
        worker.start();
    }
}
