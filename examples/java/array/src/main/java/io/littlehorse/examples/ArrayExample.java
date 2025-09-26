package io.littlehorse.examples;

import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.wfsdk.internal.structdefutil.LHClassType;
import io.littlehorse.sdk.wfsdk.internal.structdefutil.LHStructDefType;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.sdk.worker.LHTaskWorker;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArrayExample {

    private static final Logger log = LoggerFactory.getLogger(ArrayExample.class);

    public static Workflow getWorkflow() {
        return new WorkflowImpl("issue-parking-ticket", wf -> {
            WfRunVariable myArray = wf.declareArray("my-array", Integer[].class);
            WfRunVariable myStructWithArray = wf.declareStruct("my-struct", MyStruct.class);
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
        MyWorker executable = new MyWorker();

        List<LHTaskWorker> workers = new ArrayList<>();

        for (Method method : executable.getClass().getDeclaredMethods()) {
            if (!method.isAnnotationPresent(LHTaskMethod.class)) continue;

            LHTaskMethod lhTaskMethod = method.getAnnotation(LHTaskMethod.class);

            workers.add(new LHTaskWorker(executable, lhTaskMethod.value(), config));
        }

        // Gracefully shutdown
        Runtime.getRuntime()
                .addShutdownHook(new Thread(() -> workers.forEach(worker -> {
                    log.debug("Closing {}", worker.getTaskDefName());
                    worker.close();
                })));
        return workers;
    }

    public static void main(String[] args) throws IOException {
        // Let's prepare the configurations
        Properties props = getConfigProps();
        LHConfig config = new LHConfig(props);

        // New workflow
        Workflow workflow = getWorkflow();

        // System.out.println(workflow.compileWfToJson());

        LHStructDefType lhStructDefType = new LHStructDefType(MyStruct.class);

        // System.out.println(lhStructDefType.getInlineStructDef());

        String[] myArray = {"hello", "world"};

        System.out.println(LHLibUtil.objToVarVal(new MyStruct(myArray)));

        // // New worker
        // List<LHTaskWorker> workers = getTaskWorkers(config);

        // // Register tasks
        // for (LHTaskWorker worker : workers) {
        //     worker.registerTaskDef();
        // }

        // // Register a workflow
        // workflow.registerWfSpec(config.getBlockingStub());

        // // Run the workers
        // for (LHTaskWorker worker : workers) {
        //     worker.start();
        // }
    }
}
