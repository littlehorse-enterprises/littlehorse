package io.littlehorse.examples;

import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * In this example you will see how to use conditionals.
 * It will execute an "if" or "else" depending on the value of "bar".
 * If bar is greater than 10 then execute task-b else execute task-c.
 */
public class ConditionalsExample {

    private static final Logger log = LoggerFactory.getLogger(ConditionalsExample.class);

    public static Workflow getWorkflow() {
        return new WorkflowImpl("example-conditionals", wf -> {
            WfRunVariable isVip = wf.declareBool("is-vip");
            WfRunVariable price = wf.declareInt("price");
            WfRunVariable companyName = wf.declareStr("company-name");
            WfRunVariable jsonArr = wf.declareStr("special-companies").withDefault(new ArrayList<>());
            jsonArr.add("CocaCola");
            jsonArr.add("Starbucks");
            jsonArr.add("McDonalds");
            jsonArr.add("Pizza Hut");
            jsonArr.add("KFC");
            jsonArr.add("Burger King");

            // If user is VIP and price > 10000, send special welcome
            wf.doIf(isVip.and(companyName.isIn(jsonArr)), handler -> {
                handler.execute("send-special-welcome");
            });

            // If user is VIP or price == 10000, use expedited shipping
            wf.doIfElse(
                    isVip.or(price.isGreaterThan(10000)),
                    handler -> {
                        handler.execute("use-expedited-shipping");
                    },
                    handler -> {
                        handler.execute("regular-shipping");
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
        return props;
    }

    public static List<LHTaskWorker> getTaskWorkers(LHConfig config) {
        ConditionalsTaskWorker executable = new ConditionalsTaskWorker();
        List<LHTaskWorker> workers = List.of(
                new LHTaskWorker(executable, "send-special-welcome", config),
                new LHTaskWorker(executable, "use-expedited-shipping", config),
                new LHTaskWorker(executable, "regular-shipping", config),
                new LHTaskWorker(executable, "task-d", config));

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
        LittleHorseBlockingStub client = config.getBlockingStub();

        // New workflow
        Workflow workflow = getWorkflow();

        // New workers
        List<LHTaskWorker> workers = getTaskWorkers(config);

        // Register tasks
        for (LHTaskWorker worker : workers) {
            worker.registerTaskDef();
        }

        // Register a workflow
        workflow.registerWfSpec(client);

        // Run the workers
        for (LHTaskWorker worker : workers) {
            log.debug("Starting {}", worker.getTaskDefName());
            worker.start();
        }
    }
}
