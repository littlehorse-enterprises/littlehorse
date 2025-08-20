package io.littlehorse.examples;

import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.AwaitWorkflowEventRequest;
import io.littlehorse.sdk.common.proto.PutWorkflowEventDefRequest;
import io.littlehorse.sdk.common.proto.RunWfRequest;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.WfRunId;
import io.littlehorse.sdk.common.proto.WorkflowEvent;
import io.littlehorse.sdk.common.proto.WorkflowEventDefId;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
/*
 * This is a simple example, which does two things:
 * 1. Declare an "input-name" variable of type String
 * 2. Pass that variable into the execution of the "greet" task.
 */
public class AwaitWorkflowEventExample {

    public static Workflow getWorkflow() {
        return new WorkflowImpl(
            "await-wf-event",
            wf -> {
                WfRunVariable sleepTime = wf.addVariable("sleep-time", VariableType.INT).required();
                wf.sleepSeconds(sleepTime);
                wf.throwEvent("sleep-done", "hello there!");
            }
        );
    }

    public static Properties getConfigProps() throws IOException {
        Properties props = new Properties();
        File configPath = Path.of(
            System.getProperty("user.home"),
            ".config/littlehorse.config"
        ).toFile();
        if(configPath.exists()){
            props.load(new FileInputStream(configPath));
        }
        return props;
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        // Let's prepare the configurations
        Properties props = getConfigProps();
        LHConfig config = new LHConfig(props);
        LittleHorseBlockingStub client = config.getBlockingStub();

        // New workflow
        Workflow workflow = getWorkflow();

        client.putWorkflowEventDef(PutWorkflowEventDefRequest.newBuilder()
                .setName("sleep-done")
                .build());

        // Register a workflow
        workflow.registerWfSpec(config.getBlockingStub());

        int delayMs = Integer.valueOf(args[0]);
        int timeoutMs = Integer.valueOf(args[1]);
        int sleepSeconds = Integer.valueOf(args[2]);

        Thread.sleep(1000);

        String id = UUID.randomUUID().toString().replaceAll("-", "");

        System.out.println("Running workflow with id: " + id);
        client.runWf(RunWfRequest.newBuilder()
                .setWfSpecName("await-wf-event")
                .setId(id)
                .putVariables("sleep-time", LHLibUtil.objToVarVal(sleepSeconds))
                .build());

        System.out.println("Sleeping for " + delayMs + " milliseconds");
        Thread.sleep(delayMs);

        System.out.println("Now awaiting workflow event with timeout of " + timeoutMs + " milliseconds");
        WorkflowEvent event = client.withDeadlineAfter(timeoutMs, TimeUnit.MILLISECONDS)
                .awaitWorkflowEvent(AwaitWorkflowEventRequest.newBuilder()
                        .setWfRunId(WfRunId.newBuilder().setId(id))
                        .build());

        System.out.println(LHLibUtil.protoToJson(event));
    }
}
