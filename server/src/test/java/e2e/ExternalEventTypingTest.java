package e2e;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.grpc.Status.Code;
import io.grpc.StatusRuntimeException;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.proto.ExternalEventDefId;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.PutExternalEventRequest;
import io.littlehorse.sdk.common.proto.WfRunId;
import io.littlehorse.sdk.wfsdk.ExternalEventNodeOutput;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.LHWorkflow;
import io.littlehorse.test.WorkflowVerifier;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

@LHTest
public class ExternalEventTypingTest {

    @LHWorkflow("typed-external-event")
    public Workflow typedExternalEvent;

    private LittleHorseBlockingStub client;
    private WorkflowVerifier verifier;

    @LHWorkflow("typed-external-event")
    public Workflow getBasicExternalEventWorkflow() {
        return Workflow.newWorkflow("typed-external-event", thread -> {
            WfRunVariable evtOutput = thread.declareInt("evt-output");
            ExternalEventNodeOutput payload =
                    thread.waitForEvent("typed-as-int").registeredAs(Integer.class);
            evtOutput.assign(payload);
        });
    }

    @Test
    void shouldRejectStringPayloadForIntegerEvent() {
        WfRunId id = verifier.prepareRun(typedExternalEvent).start();

        assertThatThrownBy(() -> {
                    client.putExternalEvent(PutExternalEventRequest.newBuilder()
                            .setExternalEventDefId(
                                    ExternalEventDefId.newBuilder().setName("typed-external-event"))
                            .setWfRunId(id)
                            .setContent(LHLibUtil.objToVarVal("not-an-integer"))
                            .build());
                })
                .matches(exn -> {
                    return exn instanceof StatusRuntimeException
                            && ((StatusRuntimeException) exn).getStatus().getCode() == Code.INVALID_ARGUMENT
                            && exn.getMessage().toLowerCase().contains("typed-external-event");
                });
    }

    @Test
    void shouldAllowIntegerEvent() {
        verifier.prepareRun(typedExternalEvent)
                .thenSendExternalEventWithContent("typed-as-int", 12345)
                .waitForStatus(LHStatus.COMPLETED)
                .thenVerifyVariable(0, "evt-output", variable -> {
                    Assertions.assertThat(variable.getInt()).isEqualTo(12345);
                })
                .start();
    }
}
