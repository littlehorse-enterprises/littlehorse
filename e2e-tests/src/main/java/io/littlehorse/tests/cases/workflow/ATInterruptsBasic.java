package io.littlehorse.tests.cases.workflow;

import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.LHPublicApiGrpc.LHPublicApiBlockingStub;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.VariableMutationType;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.util.Arg;
import io.littlehorse.sdk.wfsdk.ThreadBuilder;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.tests.TestFailure;
import io.littlehorse.tests.WorkflowLogicTest;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class ATInterruptsBasic extends WorkflowLogicTest {

    public ATInterruptsBasic(LHPublicApiBlockingStub client, LHConfig workerConfig) {
        super(client, workerConfig);
    }

    public String getDescription() {
        return ("Tests behavior of interrupts, including zero, one, and two "
                + "stacked interrupts sent to one WfRun.");
    }

    private static final String INTERRUPT_NAME = "at-my-interrupt-event";

    public Workflow getWorkflowImpl() {
        return new WorkflowImpl(getWorkflowName(), thread -> {
            WfRunVariable sharedVar = thread.addVariable("my-int", VariableType.INT);

            thread.registerInterruptHandler(INTERRUPT_NAME, handler -> {
                WfRunVariable interruptInput = handler.addVariable(ThreadBuilder.HANDLER_INPUT_VAR, VariableType.INT);

                handler.execute("at-obiwan");

                handler.mutate(sharedVar, VariableMutationType.ADD, interruptInput);
            });

            thread.sleepSeconds(1);
            thread.execute("at-obiwan");
        });
    }

    public List<Object> getTaskWorkerObjects() {
        return Arrays.asList(new ATSimpleTask());
    }

    public List<String> launchAndCheckWorkflows(LHPublicApiBlockingStub client)
            throws TestFailure, InterruptedException, IOException {
        return Arrays.asList(
                runWithoutInterrupt(client),
                sendEventBefore(client),
                oneInterrupt(client),
                twoInterrupts(client),
                invalidEventTypeShouldFail(client));
    }

    private String runWithoutInterrupt(LHPublicApiBlockingStub client)
            throws TestFailure, InterruptedException, IOException {
        String id = runWf(client, Arg.of("my-int", 5));
        assertStatus(client, id, LHStatus.RUNNING);
        Thread.sleep(3 * 1000);
        assertStatus(client, id, LHStatus.COMPLETED);
        assertVarEqual(client, id, 0, "my-int", 5);
        return id;
    }

    private String sendEventBefore(LHPublicApiBlockingStub client)
            throws TestFailure, InterruptedException, IOException {
        String id = generateGuid();

        // post an event before running the wf
        sendEvent(client, id, INTERRUPT_NAME, 10, null);

        runWf(id, client, Arg.of("my-int", 5));
        assertStatus(client, id, LHStatus.RUNNING);
        Thread.sleep(3 * 1000);
        assertStatus(client, id, LHStatus.COMPLETED);
        assertVarEqual(client, id, 0, "my-int", 5);
        return id;
    }

    private String twoInterrupts(LHPublicApiBlockingStub client) throws TestFailure, InterruptedException, IOException {
        String id = runWf(client, Arg.of("my-int", 5));

        sendEvent(client, id, INTERRUPT_NAME, 10, null);
        sendEvent(client, id, INTERRUPT_NAME, 10, null);
        // NOTE: Currently, two simultaneous interrupts result in two child
        // threads running simultaneously; i.e. the second interrupt doesn't
        // interfere with the first interrupt.
        // THAT MAY CHANGE in a future LH release. When that changes,
        // this unit test will fail.
        Thread.sleep(300);

        assertTaskOutputsMatch(client, id, 1, "hello there");
        assertTaskOutputsMatch(client, id, 2, "hello there");
        assertStatus(client, id, LHStatus.RUNNING);
        Thread.sleep(7 * 1000);
        assertStatus(client, id, LHStatus.COMPLETED);
        assertVarEqual(client, id, 0, "my-int", 25);
        return id;
    }

    private String oneInterrupt(LHPublicApiBlockingStub client) throws TestFailure, InterruptedException, IOException {
        String id = runWf(client, Arg.of("my-int", 5));

        sendEvent(client, id, INTERRUPT_NAME, 10, null);

        Thread.sleep(3 * 1000);

        assertStatus(client, id, LHStatus.COMPLETED);
        assertVarEqual(client, id, 0, "my-int", 15);
        assertTaskOutputsMatch(client, id, 1, "hello there");
        return id;
    }

    private String invalidEventTypeShouldFail(LHPublicApiBlockingStub client)
            throws TestFailure, InterruptedException, IOException {
        String id = runWf(client, Arg.of("my-int", 5));
        sendEvent(client, id, INTERRUPT_NAME, "bad input should crash", null);
        assertStatus(client, id, LHStatus.ERROR);
        Thread.sleep(3 * 1000);
        // should still be dead after the sleep node expires
        assertStatus(client, id, LHStatus.ERROR);
        return id;
    }
}

class ATSimpleTask {

    @LHTaskMethod("at-obiwan")
    public String obiwan() {
        return "hello there";
    }
}
