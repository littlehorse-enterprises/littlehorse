package io.littlehorse.tests.cases.workflow;

import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.ExternalEvent;
import io.littlehorse.sdk.common.proto.ExternalEventId;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.StopWfRunRequest;
import io.littlehorse.sdk.common.proto.VariableMutationType;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.tests.TestFailure;
import io.littlehorse.tests.WorkflowLogicTest;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class AOExternalEventBasic extends WorkflowLogicTest {

    public AOExternalEventBasic(LittleHorseBlockingStub client, LHConfig workerConfig) {
        super(client, workerConfig);
    }

    public String getDescription() {
        return ("Tests vanilla External Events. Checks that expected behavior works "
                + "when we send zero, one, or two events.");
    }

    public Workflow getWorkflowImpl() {
        return new WorkflowImpl(getWorkflowName(), thread -> {
            WfRunVariable evtOutput = thread.addVariable("evt-output", VariableType.STR);
            thread.mutate(evtOutput, VariableMutationType.ASSIGN, thread.waitForEvent("ao-my-event"));
            thread.execute("ao-simple", evtOutput);
        });
    }

    public List<Object> getTaskWorkerObjects() {
        return Arrays.asList(new AOSimpleTask());
    }

    public List<String> launchAndCheckWorkflows(LittleHorseBlockingStub client)
            throws TestFailure, InterruptedException, IOException {
        return Arrays.asList(
                sendEventBeforeWfRun(client),
                sendEventAfterWfRun(client),
                sendTwoEventsBefore(client),
                dontSendEvent(client));
    }

    private String sendEventBeforeWfRun(LittleHorseBlockingStub client)
            throws TestFailure, InterruptedException, IOException {
        String wfRunId = generateGuid();
        ExternalEventId extEvtId = sendEvent(client, wfRunId, "ao-my-event", "evt-content", null);
        Thread.sleep(10);
        runWf(wfRunId, client);
        Thread.sleep(3000);
        assertStatus(client, wfRunId, LHStatus.COMPLETED);

        assertVarEqual(client, wfRunId, 0, "evt-output", "evt-content");
        assertTaskOutput(client, wfRunId, 0, 2, "hello there evt-content");
        ExternalEvent evt = getExternalEvent(client, extEvtId);
        if (!evt.getId().getWfRunId().getId().equals(wfRunId)) {
            throw new TestFailure(this, "Failed to associate evt with wfRun " + wfRunId);
        }

        return wfRunId;
    }

    private String sendEventAfterWfRun(LittleHorseBlockingStub client)
            throws TestFailure, InterruptedException, IOException {
        String wfRunId = generateGuid();
        // here only difference is we run the workflow first
        runWf(wfRunId, client);
        Thread.sleep(500);

        // TODO: Maybe check more, ensure that we're waiting on the same event
        assertStatus(client, wfRunId, LHStatus.RUNNING);

        ExternalEventId extEvtId = sendEvent(client, wfRunId, "ao-my-event", "evt-content", null);
        Thread.sleep(500);

        assertStatus(client, wfRunId, LHStatus.COMPLETED);

        assertVarEqual(client, wfRunId, 0, "evt-output", "evt-content");
        assertTaskOutput(client, wfRunId, 0, 2, "hello there evt-content");
        ExternalEvent evt = getExternalEvent(client, extEvtId);
        if (!evt.getClaimed()) {
            throw new TestFailure(this, "Failed to associate evt with wfRun " + wfRunId);
        }

        return wfRunId;
    }

    private String sendTwoEventsBefore(LittleHorseBlockingStub client)
            throws TestFailure, InterruptedException, IOException {
        String wfRunId = generateGuid();
        ExternalEventId extEvtIdOne = sendEvent(client, wfRunId, "ao-my-event", "evt-content", null);
        ExternalEventId extEvtIdTwo = sendEvent(client, wfRunId, "ao-my-event", "evt-content-two", null);
        Thread.sleep(10);
        runWf(wfRunId, client);
        Thread.sleep(500);
        assertStatus(client, wfRunId, LHStatus.COMPLETED);

        assertVarEqual(client, wfRunId, 0, "evt-output", "evt-content");
        assertTaskOutput(client, wfRunId, 0, 2, "hello there evt-content");
        ExternalEvent evt = getExternalEvent(client, extEvtIdOne);
        if (!evt.hasNodeRunPosition()) {
            throw new TestFailure(this, "Failed to associate evt with wfRun " + wfRunId);
        }

        ExternalEvent evtTwo = getExternalEvent(client, extEvtIdTwo);
        if (evtTwo.hasNodeRunPosition()) {
            throw new TestFailure(this, "Should not have associated second event!");
        }

        return wfRunId;
    }

    private String dontSendEvent(LittleHorseBlockingStub client) throws TestFailure, InterruptedException, IOException {
        String wfRunId = runWf(client);
        Thread.sleep(500);
        // TODO: Inspect the node run a bit
        assertStatus(client, wfRunId, LHStatus.RUNNING);

        // This is so that we can delete it in the cleanup() method.
        client.stopWfRun(StopWfRunRequest.newBuilder()
                .setWfRunId(LHLibUtil.wfRunId(wfRunId))
                .setThreadRunNumber(0)
                .build());
        return wfRunId;
    }
}

class AOSimpleTask {

    @LHTaskMethod("ao-simple")
    public String greet(String input) {
        return "hello there " + input;
    }
}
