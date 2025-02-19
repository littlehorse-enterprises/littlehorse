package e2e;

import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.proto.ExternalEvent;
import io.littlehorse.sdk.common.proto.ExternalEventDefId;
import io.littlehorse.sdk.common.proto.ExternalEventId;
import io.littlehorse.sdk.common.proto.Failure;
import io.littlehorse.sdk.common.proto.LHErrorType;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.PutExternalEventRequest;
import io.littlehorse.sdk.common.proto.VariableMutationType;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.WfRunId;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.LHWorkflow;
import io.littlehorse.test.WorkflowVerifier;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

@LHTest(externalEventNames = {ExternalEventTest.EVT_NAME, ExternalEventTest.IGNORED_EVT_NAME})
public class ExternalEventTest {

    public static final String EVT_NAME = "basic-test-event";
    public static final String IGNORED_EVT_NAME = "not-a-real-event-kenobi";

    @LHWorkflow("external-event-timeout")
    public Workflow timeoutEvent;

    @LHWorkflow("basic-external-event")
    public Workflow basicExternalEvent;

    private LittleHorseBlockingStub client;
    private WorkflowVerifier verifier;

    @LHWorkflow("basic-external-event")
    public Workflow getBasicExternalEventWorkflow() {
        return Workflow.newWorkflow("basic-external-event", thread -> {
            WfRunVariable evtOutput = thread.addVariable("evt-output", VariableType.STR);
            thread.mutate(evtOutput, VariableMutationType.ASSIGN, thread.waitForEvent(EVT_NAME));
            thread.execute("basic-external-event-task", evtOutput);
        });
    }

    @LHWorkflow("external-event-timeout")
    public Workflow getTimeoutWorkflow() {
        return Workflow.newWorkflow("external-event-timeout", wf -> {
            wf.waitForEvent(EVT_NAME).timeout(1);
        });
    }

    @Test
    void shouldTimeoutIfNoEvent() {
        verifier.prepareRun(timeoutEvent)
                .waitForStatus(LHStatus.ERROR)
                .thenVerifyNodeRun(0, 1, nodeRun -> {
                    Failure failure = nodeRun.getFailures(0);
                    Assertions.assertThat(failure.getFailureName()).isEqualTo(LHErrorType.TIMEOUT.toString());
                    Assertions.assertThat(failure.getMessage().toLowerCase()).contains("arrive in time");
                })
                .start();
    }

    @Test
    void shouldCompleteIfEventIsSentAfterWfRunStarts() {
        WfRunId id = WfRunId.newBuilder().setId(LHUtil.generateGuid()).build();

        verifier.prepareRun(basicExternalEvent)
                .waitForNodeRunStatus(0, 1, LHStatus.RUNNING)
                .thenSendExternalEventWithContent(EVT_NAME, "hello there")
                .waitForStatus(LHStatus.COMPLETED)
                .thenVerifyTaskRunResult(0, 2, variableValue -> {
                    Assertions.assertThat(variableValue.getStr()).isEqualTo("hello there");
                })
                .start(id);
    }

    @Test
    void shouldCompleteWhenWeSendEventBeforeWfRun() {
        // We post an external event before the WfRun is created. It should complete.
        WfRunId id = WfRunId.newBuilder().setId(LHUtil.generateGuid()).build();

        client.putExternalEvent(PutExternalEventRequest.newBuilder()
                .setContent(LHLibUtil.objToVarVal("hello there"))
                .setWfRunId(id)
                .setExternalEventDefId(ExternalEventDefId.newBuilder().setName(EVT_NAME))
                .build());

        verifier.prepareRun(basicExternalEvent)
                .waitForStatus(LHStatus.COMPLETED)
                .thenVerifyTaskRunResult(0, 2, variableValue -> {
                    Assertions.assertThat(variableValue.getStr()).isEqualTo("hello there");
                })
                .start(id);
    }

    @Test
    void shouldIgnoreEventsFromWrongExternalEventDef() {
        WfRunId id = WfRunId.newBuilder().setId(LHUtil.generateGuid()).build();

        client.putExternalEvent(PutExternalEventRequest.newBuilder()
                .setWfRunId(id)
                .setExternalEventDefId(ExternalEventDefId.newBuilder().setName(IGNORED_EVT_NAME))
                .build());

        verifier.prepareRun(basicExternalEvent)
                .thenSendExternalEventWithContent(IGNORED_EVT_NAME, "hello there")
                .thenVerifyWfRun(wfRun -> {
                    Assertions.assertThat(wfRun.getStatus()).isEqualTo(LHStatus.RUNNING);
                })
                .thenSendExternalEventWithContent(EVT_NAME, "kenobi")
                .waitForStatus(LHStatus.COMPLETED)
                .thenVerifyTaskRunResult(0, 2, variableValue -> {
                    Assertions.assertThat(variableValue.getStr()).isEqualTo("kenobi");
                })
                .start(id);
    }

    @Test
    void shouldOnlyClaimOneEventWhenTwoAreSentBefore() {
        String firstEventContent = "first";
        String firstEventGuid = "fdsa";
        String secondEventPayload = "second";

        // the second event guid is alphabetically before the first. This is intentional:
        // we need the range scan to go by created date not by guid.
        String secondEventGuid = "asdf";

        WfRunId id = WfRunId.newBuilder().setId(LHUtil.generateGuid()).build();

        client.putExternalEvent(PutExternalEventRequest.newBuilder()
                .setContent(LHLibUtil.objToVarVal(firstEventContent))
                .setWfRunId(id)
                .setGuid(firstEventGuid)
                .setExternalEventDefId(ExternalEventDefId.newBuilder().setName(EVT_NAME))
                .build());

        client.putExternalEvent(PutExternalEventRequest.newBuilder()
                .setContent(LHLibUtil.objToVarVal(secondEventPayload))
                .setGuid(secondEventGuid)
                .setWfRunId(id)
                .setExternalEventDefId(ExternalEventDefId.newBuilder().setName(EVT_NAME))
                .build());

        verifier.prepareRun(basicExternalEvent)
                .waitForStatus(LHStatus.COMPLETED)
                .thenVerifyTaskRunResult(0, 2, variableValue -> {
                    // The first event we sent should be claimed, not the second.
                    Assertions.assertThat(variableValue.getStr()).isEqualTo(firstEventContent);
                })
                .start(id);

        // The first ExternalEvent should be claimed and the second should not.
        ExternalEvent first = client.getExternalEvent(ExternalEventId.newBuilder()
                .setWfRunId(id)
                .setExternalEventDefId(ExternalEventDefId.newBuilder().setName(EVT_NAME))
                .setGuid(firstEventGuid)
                .build());
        Assertions.assertThat(first.getClaimed()).isTrue();

        ExternalEvent second = client.getExternalEvent(ExternalEventId.newBuilder()
                .setWfRunId(id)
                .setExternalEventDefId(ExternalEventDefId.newBuilder().setName(EVT_NAME))
                .setGuid(secondEventGuid)
                .build());
        Assertions.assertThat(second.getClaimed()).isFalse();
    }

    @LHTaskMethod("basic-external-event-task")
    public String doThing(String input) {
        return input;
    }
}
