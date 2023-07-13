package io.littlehorse.jtests.test.workflowtests;

import io.littlehorse.jtests.test.LogicTestFailure;
import io.littlehorse.jtests.test.WorkflowLogicTest;
import io.littlehorse.sdk.client.LHClient;
import io.littlehorse.sdk.common.config.LHWorkerConfig;
import io.littlehorse.sdk.common.exception.LHApiError;
import io.littlehorse.sdk.common.proto.LHStatusPb;
import io.littlehorse.sdk.common.proto.VariableMutationTypePb;
import io.littlehorse.sdk.common.proto.VariableTypePb;
import io.littlehorse.sdk.common.util.Arg;
import io.littlehorse.sdk.wfsdk.SpawnedThread;
import io.littlehorse.sdk.wfsdk.ThreadBuilder;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskMethod;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class AVChildThreadInterrupt extends WorkflowLogicTest {

    public AVChildThreadInterrupt(LHClient client, LHWorkerConfig workerConfig) {
        super(client, workerConfig);
    }

    public String getDescription() {
        return (
            "Tests that we can interrupt a child thread without interrupting " +
            "the parent thread."
        );
    }

    private static final String CHILD_EVENT = "child-event";
    private static final String PARENT_EVENT = "parent-event";

    public Workflow getWorkflowImpl() {
        return new WorkflowImpl(
            getWorkflowName(),
            thread -> {
                WfRunVariable parentInt = thread.addVariable(
                    "parent-int",
                    VariableTypePb.INT
                );

                thread.registerInterruptHandler(
                    PARENT_EVENT,
                    parentHandler -> {
                        WfRunVariable interruptInput = parentHandler.addVariable(
                            ThreadBuilder.HANDLER_INPUT_VAR,
                            VariableTypePb.INT
                        );

                        parentHandler.execute("av-obiwan");
                        parentHandler.mutate(
                            parentInt,
                            VariableMutationTypePb.ADD,
                            interruptInput
                        );
                        parentHandler.sleepSeconds(3);
                    }
                );

                SpawnedThread childThread = thread.spawnThread(
                    child -> {
                        WfRunVariable childInt = child.addVariable(
                            "child-int",
                            VariableTypePb.INT
                        );

                        child.registerInterruptHandler(
                            CHILD_EVENT,
                            childHandler -> {
                                WfRunVariable interruptInput = childHandler.addVariable(
                                    ThreadBuilder.HANDLER_INPUT_VAR,
                                    VariableTypePb.INT
                                );
                                childHandler.execute("av-obiwan");
                                childHandler.sleepSeconds(3);
                                childHandler.mutate(
                                    childInt,
                                    VariableMutationTypePb.ADD,
                                    interruptInput
                                );
                            }
                        );

                        child.execute("av-obiwan");
                        child.sleepSeconds(3);
                    },
                    "child",
                    Map.of("child-int", 0)
                );

                thread.sleepSeconds(3);
                thread.execute("av-obiwan");
                thread.waitForThreads(childThread);
            }
        );
    }

    public List<Object> getTaskWorkerObjects() {
        return Arrays.asList(new AVSimpleTask());
    }

    public List<String> launchAndCheckWorkflows(LHClient client)
        throws LogicTestFailure, InterruptedException, LHApiError {
        return Arrays.asList(
            runWithNoInterrupts(client),
            interruptChild(client),
            interruptParent(client),
            interruptBoth(client)
        );
    }

    private String runWithNoInterrupts(LHClient client)
        throws LogicTestFailure, InterruptedException, LHApiError {
        String wfRunId = runWf(client, Arg.of("parent-int", 0));
        Thread.sleep(1000 * 8);
        assertStatus(client, wfRunId, LHStatusPb.COMPLETED);
        assertTaskOutputsMatch(client, wfRunId, 0, "hello there");
        assertTaskOutputsMatch(client, wfRunId, 1, "hello there");
        assertVarEqual(client, wfRunId, 0, "parent-int", 0);
        assertVarEqual(client, wfRunId, 1, "child-int", 0);
        return wfRunId;
    }

    private String interruptChild(LHClient client)
        throws LogicTestFailure, InterruptedException, LHApiError {
        String wfRunId = runWf(client, Arg.of("parent-int", 0));

        sendEvent(client, wfRunId, CHILD_EVENT, 10, null);
        Thread.sleep(1000 * 1);
        assertStatus(client, wfRunId, LHStatusPb.RUNNING);
        assertThreadStatus(client, wfRunId, 0, LHStatusPb.RUNNING);
        assertThreadStatus(client, wfRunId, 1, LHStatusPb.HALTED);
        assertThreadStatus(client, wfRunId, 2, LHStatusPb.RUNNING);

        Thread.sleep(1000 * 12);

        assertStatus(client, wfRunId, LHStatusPb.COMPLETED);
        assertTaskOutputsMatch(client, wfRunId, 0, "hello there");
        assertTaskOutputsMatch(client, wfRunId, 1, "hello there");
        assertTaskOutputsMatch(client, wfRunId, 2, "hello there");
        assertVarEqual(client, wfRunId, 0, "parent-int", 0);
        assertVarEqual(client, wfRunId, 1, "child-int", 10);
        return wfRunId;
    }

    private String interruptParent(LHClient client)
        throws LogicTestFailure, InterruptedException, LHApiError {
        String wfRunId = runWf(client, Arg.of("parent-int", 0));

        sendEvent(client, wfRunId, PARENT_EVENT, 10, null);
        Thread.sleep(1000 * 1);
        assertStatus(client, wfRunId, LHStatusPb.RUNNING);

        assertThreadStatus(client, wfRunId, 0, LHStatusPb.HALTED);
        assertThreadStatus(client, wfRunId, 1, LHStatusPb.HALTED);
        assertThreadStatus(client, wfRunId, 2, LHStatusPb.RUNNING);

        Thread.sleep(1000 * 15);

        assertStatus(client, wfRunId, LHStatusPb.COMPLETED);
        assertTaskOutputsMatch(client, wfRunId, 0, "hello there");
        assertTaskOutputsMatch(client, wfRunId, 1, "hello there");
        assertTaskOutputsMatch(client, wfRunId, 2, "hello there");
        assertVarEqual(client, wfRunId, 0, "parent-int", 10);
        assertVarEqual(client, wfRunId, 1, "child-int", 0);
        return wfRunId;
    }

    private String interruptBoth(LHClient client)
        throws LogicTestFailure, InterruptedException, LHApiError {
        String wfRunId = runWf(client, Arg.of("parent-int", 0));

        sendEvent(client, wfRunId, PARENT_EVENT, 10, null);
        sendEvent(client, wfRunId, CHILD_EVENT, 10, null);
        Thread.sleep(1000 * 1);
        assertStatus(client, wfRunId, LHStatusPb.RUNNING);
        assertThreadStatus(client, wfRunId, 0, LHStatusPb.HALTED);
        assertThreadStatus(client, wfRunId, 1, LHStatusPb.HALTED);

        // TODO: In the future, there may be a LittleHorse update which enforces
        // that there can only be one RUNNING interrupt thread at a time.
        assertThreadStatus(client, wfRunId, 2, LHStatusPb.RUNNING);
        assertThreadStatus(client, wfRunId, 3, LHStatusPb.RUNNING);

        Thread.sleep(1000 * 15);

        assertStatus(client, wfRunId, LHStatusPb.COMPLETED);
        assertTaskOutputsMatch(client, wfRunId, 0, "hello there");
        assertTaskOutputsMatch(client, wfRunId, 1, "hello there");
        assertTaskOutputsMatch(client, wfRunId, 2, "hello there");
        assertVarEqual(client, wfRunId, 0, "parent-int", 10);
        assertVarEqual(client, wfRunId, 1, "child-int", 10);

        return wfRunId;
    }
}

class AVSimpleTask {

    @LHTaskMethod("av-obiwan")
    public String obiwan() {
        return "hello there";
    }
}
