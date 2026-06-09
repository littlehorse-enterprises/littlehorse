package io.littlehorse.examples;

import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.PutExternalEventDefRequest;
import io.littlehorse.sdk.common.proto.PutWorkflowEventDefRequest;
import io.littlehorse.sdk.usertask.UserTaskSchema;
import io.littlehorse.sdk.wfsdk.NodeOutput;
import io.littlehorse.sdk.wfsdk.SpawnedChildWf;
import io.littlehorse.sdk.wfsdk.SpawnedThread;
import io.littlehorse.sdk.wfsdk.SpawnedThreads;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.WorkflowThread;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskWorker;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/*
 * Dashboard node gallery: exercises every node shape rendered by the diagram UI.
 */
public class BasicExample {

    private static final String WF_NAME = "example-basic";
    private static final String CHILD_WF_NAME = "example-basic-child";
    private static final String WORKFLOW_EVENT_NAME = "basic-done";
    private static final String EXTERNAL_EVENT_NAME = "name-event";
    private static final String USER_TASK_NAME = "basic-approval";

    public static Workflow getChildWorkflow() {
        return new WorkflowImpl(CHILD_WF_NAME, wf -> {
            WfRunVariable childInput = wf.declareStr("child-input-name").required();
            wf.complete(wf.execute("greet", childInput));
        });
    }

    public static Workflow getWorkflow() {
        return new WorkflowImpl(WF_NAME, wf -> {
            WfRunVariable theName = wf.declareStr("input-name").searchable();
            WfRunVariable ready = wf.declareBool("ready").withDefault(true);
            WfRunVariable loopCount = wf.declareInt("loop-count").withDefault(1);
            WfRunVariable approvalChain = wf.declareJsonObj("approval-chain")
                    .withDefault(Map.of("description", "done", "approvals", List.of(Map.of("user", "alice"))));

            wf.sleepSeconds(1);
            wf.waitForCondition(ready.isEqualTo(true));

            NodeOutput greeting = wf.execute("greet", theName);
            wf.doIf(greeting.isEqualTo("hello there, Obi-Wan"), ifHandler -> ifHandler.execute("greet", theName))
                    .doElse(elseHandler -> elseHandler.execute("greet", "unknown"));

            wf.doWhile(loopCount.isGreaterThan(0), handler -> {
                loopCount.assign(loopCount.subtract(1));
                handler.execute("greet", loopCount);
            });

            wf.spawnThread(
                    child -> {
                        WfRunVariable eventName = child.declareStr("event-name");
                        eventName.assign(child.waitForEvent(EXTERNAL_EVENT_NAME));
                        child.execute("greet", eventName);
                    },
                    "event-waiter",
                    null);

            wf.spawnThread(
                    child -> child.assignUserTask(USER_TASK_NAME, theName, "reviewers"), "approval-thread", null);

            SpawnedThreads foreachThreads = wf.spawnThreadForEach(
                    approvalChain.jsonPath("$.approvals"),
                    "foreach-thread",
                    inner -> {
                        inner.declareInt("foreach-slot");
                        WfRunVariable input = inner.declareJsonObj(WorkflowThread.HANDLER_INPUT_VAR);
                        inner.execute("greet", input.jsonPath("$.user"));
                    },
                    Map.of("foreach-slot", 0));

            SpawnedThread helper = wf.spawnThread(child -> child.execute("greet", "helper"), "helper-thread", null);

            wf.waitForThreads(foreachThreads);
            wf.waitForThreads(SpawnedThreads.of(helper));

            SpawnedChildWf childWf = wf.runWf(CHILD_WF_NAME, Map.of("child-input-name", theName));
            wf.waitForChildWf(childWf);

            wf.throwEvent(WORKFLOW_EVENT_NAME, greeting);
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
        LHTaskWorker worker = new LHTaskWorker(executable, "greet", config);

        Runtime.getRuntime().addShutdownHook(new Thread(worker::close));
        return worker;
    }

    public static void main(String[] args) throws IOException {
        Properties props = getConfigProps();
        LHConfig config = new LHConfig(props);
        LittleHorseBlockingStub client = config.getBlockingStub();

        Workflow childWorkflow = getChildWorkflow();
        Workflow workflow = getWorkflow();
        LHTaskWorker worker = getTaskWorker(config);

        client.putWorkflowEventDef(PutWorkflowEventDefRequest.newBuilder()
                .setName(WORKFLOW_EVENT_NAME)
                .build());

        Set<String> externalEventNames = workflow.getRequiredExternalEventDefNames();
        for (String externalEventName : externalEventNames) {
            client.putExternalEventDef(PutExternalEventDefRequest.newBuilder()
                    .setName(externalEventName)
                    .build());
        }

        UserTaskSchema approvalForm = new UserTaskSchema(new BasicApprovalForm(), USER_TASK_NAME);
        client.putUserTaskDef(approvalForm.compile());

        childWorkflow.registerWfSpec(client);
        workflow.registerWfSpec(client);

        try {
            worker.registerTaskDef();
        } catch (Exception ignored) {
            // greet TaskDef is immutable once registered
        }

        worker.start();
    }
}
