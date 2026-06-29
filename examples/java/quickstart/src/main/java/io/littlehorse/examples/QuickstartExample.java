package io.littlehorse.examples;

import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.worker.LHTaskWorker;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

public class QuickstartExample {

    private static final KnowYourCustomerTasks TASKS = new KnowYourCustomerTasks();

    public static Properties getConfigProps() throws IOException {
        Properties props = new Properties();
        File configPath = Path.of(System.getProperty("user.home"), ".config/littlehorse.config")
                .toFile();
        if (configPath.exists()) {
            props.load(new FileInputStream(configPath));
        }
        return props;
    }

    public static List<LHTaskWorker> getTaskWorkers(LHConfig config) {
        return List.of(
                new LHTaskWorker(TASKS, QuickstartWorkflow.VERIFY_IDENTITY_TASK, config),
                new LHTaskWorker(TASKS, QuickstartWorkflow.NOTIFY_CUSTOMER_VERIFIED_TASK, config),
                new LHTaskWorker(TASKS, QuickstartWorkflow.NOTIFY_CUSTOMER_NOT_VERIFIED_TASK, config));
    }

    public static void main(String[] args) throws IOException {
        LHConfig config = new LHConfig(getConfigProps());
        Workflow workflow = new QuickstartWorkflow().getWorkflow();
        List<LHTaskWorker> workers = getTaskWorkers(config);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> workers.forEach(LHTaskWorker::close)));

        workers.forEach(LHTaskWorker::registerTaskDef);
        workflow.registerWfSpec(config.getBlockingStub());

        System.out.println("Registered quickstart metadata and starting task workers.");
        workers.forEach(LHTaskWorker::start);
    }
}
