package io.littlehorse.examples;

import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskWorker;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;

/*
 * This example demonstrates how to use expressions in LittleHorse workflows.
 * It defines a workflow that calculates the total price of an order based on quantity, price, and taxes.
 * The `placeOrder` task is executed with the calculated total.
 * The `MyWorker` class contains the task method that processes the order.
 */
public class ExpressionsExample {

    public static Workflow getWorkflow() {
        return new WorkflowImpl("example-expressions", wf -> {
            var quantity = wf.declareInt("quantity");
            var price = wf.declareDouble("price");
            var taxes = wf.declareDouble("taxes");
            wf.execute("place-order", quantity.multiply(price.multiply(wf.add(1, taxes.divide(100.0)))));
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

    public static LHTaskWorker getTaskWorker(LHConfig config) {
        MyWorker executable = new MyWorker();
        LHTaskWorker worker = new LHTaskWorker(executable, "place-order", config);

        // Gracefully shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(worker::close));
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
