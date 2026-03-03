package io.littlehorse.examples;

import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.sdk.worker.LHTaskWorker;
import io.littlehorse.sdk.worker.adapter.LHStringAdapter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TypeAdapterExample {

    private static final Logger log = LoggerFactory.getLogger(TypeAdapterExample.class);

    public static Workflow getWorkflow() {
        return new WorkflowImpl("example-type-adapter", wf -> {
            WfRunVariable uuidVar = wf.declareStr("uuid").searchable();
            uuidVar.assign(wf.execute("get-uuid"));
            wf.execute("echo-uuid", uuidVar);
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

    public static List<LHTaskWorker> getTaskWorkers(LHConfig config) {
        Worker executable = new Worker();
        List<LHTaskWorker> workers = new ArrayList<>();

        for (Method method : executable.getClass().getDeclaredMethods()) {
            if (!method.isAnnotationPresent(LHTaskMethod.class)) {
                continue;
            }

            LHTaskMethod lhTaskMethod = method.getAnnotation(LHTaskMethod.class);
            workers.add(new LHTaskWorker(executable, lhTaskMethod.value(), config));
        }

        Runtime.getRuntime()
                .addShutdownHook(new Thread(() -> workers.forEach(worker -> {
                    log.debug("Closing {}", worker.getTaskDefName());
                    worker.close();
                })));

        return workers;
    }
    
    public void registerTypeAdapters(LHConfig config) {
        config.registerTypeAdapter(new LHStringAdapter<UUID>() {
            @Override
            public String toString(UUID src) {
                return src.toString();
            }

            @Override
            public UUID fromString(String src) {
                return UUID.fromString(src);
            }
        });
    }

    public static void main(String[] args) throws IOException {
        Properties props = getConfigProps();
        LHConfig config = new LHConfig(props);

        registerTypeAdapters(config);

        Workflow workflow = getWorkflow();
        List<LHTaskWorker> workers = getTaskWorkers(config);

        for (LHTaskWorker worker : workers) {
            worker.registerTaskDef();
        }

        workflow.registerWfSpec(config.getBlockingStub());

        for (LHTaskWorker worker : workers) {
            log.debug("Starting {}", worker.getTaskDefName());
            worker.start();
        }
    }
}
