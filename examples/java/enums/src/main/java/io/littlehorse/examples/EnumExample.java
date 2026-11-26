package io.littlehorse.examples;

import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.worker.LHTaskWorker;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnumExample {

    private static final Logger log = LoggerFactory.getLogger(EnumExample.class);

    public static Workflow getWorkflow(LHConfig config) {
        return Workflow.newWorkflow(
                "example-enums",
                wf -> {
                    WfRunVariable summary = wf.declareStr("summary").required();
                    WfRunVariable priority = wf.declareInlineEnum("priority", Priority.class);

                    priority.assign(wf.execute("classify-priority", summary));

                    // The server compares the backing values. HIGH (10) and CRITICAL (11) are
                    // greater than MEDIUM (1).
                    wf.doIf(
                                    priority.isGreaterThan(Priority.MEDIUM),
                                    then -> then.execute("page-on-call", summary, priority))
                            .doElse(orElse -> orElse.execute("add-to-queue", summary, priority.name()));
                },
                config);
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

    public static List<LHTaskWorker> getTaskWorkers(LHConfig config) {
        EnumWorker executable = new EnumWorker();
        List<LHTaskWorker> workers = List.of(
                new LHTaskWorker(executable, "classify-priority", config),
                new LHTaskWorker(executable, "page-on-call", config),
                new LHTaskWorker(executable, "add-to-queue", config));

        Runtime.getRuntime()
                .addShutdownHook(new Thread(() -> workers.forEach(worker -> {
                    log.debug("Closing {}", worker.getTaskDefName());
                    worker.close();
                })));
        return workers;
    }

    public static void main(String[] args) throws IOException {
        LHConfig config = new LHConfig(getConfigProps());
        Workflow workflow = getWorkflow(config);
        List<LHTaskWorker> workers = getTaskWorkers(config);

        for (LHTaskWorker worker : workers) {
            worker.registerTaskDef();
        }

        workflow.registerWfSpec(config.getBlockingStub());

        for (LHTaskWorker worker : workers) {
            log.info("Starting {}", worker.getTaskDefName());
            worker.start();
        }
    }
}
