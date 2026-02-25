package io.littlehorse.examples;

import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.Comparator;
import io.littlehorse.sdk.common.proto.PutExternalEventDefRequest;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;

/*
 * This is a wait for condition example, which does three things:
 * 1. Declare a "counter" variable of type Integer
 * 2. Waits until the counter reaches 0.
 * 3. An interrupt handler which decrements the counter variable when an external event is received.
 */
public class WaitForConditionExample {
    private static final String INTERRUPT_NAME = "subtract";

    public static Workflow getWorkflow() {

        return new WorkflowImpl("example-wait-for-condition", wf -> {
            WfRunVariable counter = wf.declareInt("counter").withDefault(2);

            wf.waitForCondition(wf.condition(counter, Comparator.EQUALS, 0));

            // Interrupt handler which mutates the parent variable
            wf.registerInterruptHandler(INTERRUPT_NAME, handler -> {
                counter.assign(counter.subtract(1));
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

    public static void main(String[] args) throws IOException {
        // Let's prepare the configurations
        Properties props = getConfigProps();
        LHConfig config = new LHConfig(props);
        var client = config.getBlockingStub();

        client.putExternalEventDef(
                PutExternalEventDefRequest.newBuilder().setName(INTERRUPT_NAME).build());
        // New workflow
        Workflow workflow = getWorkflow();

        // Register a workflow
        workflow.registerWfSpec(config.getBlockingStub());
    }
}
