package io.littlehorse.examples;

import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.StructDefCompatibilityType;
import io.littlehorse.sdk.worker.LHTaskWorker;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

public class InlineWorkflowExample {

    public static void main(String[] args) throws IOException {
        Properties properties = new Properties();
        Path configPath = Path.of(System.getProperty("user.home"), ".config/littlehorse.config");
        if (Files.exists(configPath)) {
            try (InputStream configFile = Files.newInputStream(configPath)) {
                properties.load(configFile);
            }
        }
        LHConfig config = new LHConfig(properties);
        InlineWorkflowTasks tasks = new InlineWorkflowTasks();
        LHTaskWorker personWorker = new LHTaskWorker(tasks, "inline-greet-person", config);
        List<LHTaskWorker> workers = List.of(
                new LHTaskWorker(tasks, "inline-greet", config),
                new LHTaskWorker(tasks, "inline-add", config),
                personWorker);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> workers.forEach(LHTaskWorker::close)));

        // Register the input schema before the TaskDef that references it.
        personWorker.registerStructDef(Person.class, StructDefCompatibilityType.NO_SCHEMA_UPDATES);

        // No WfSpec registration: lhctl will build and run the inline workflows.
        for (LHTaskWorker worker : workers) {
            worker.registerTaskDef();
        }
        for (LHTaskWorker worker : workers) {
            worker.start();
        }
    }
}
