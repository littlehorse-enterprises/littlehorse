package e2e;

import io.grpc.Status.Code;
import io.grpc.StatusRuntimeException;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.proto.CorrelatedEvent;
import io.littlehorse.sdk.common.proto.CorrelatedEventConfig;
import io.littlehorse.sdk.common.proto.CorrelatedEventId;
import io.littlehorse.sdk.common.proto.ExternalEventDefId;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.PutCorrelatedEventRequest;
import io.littlehorse.sdk.common.util.Arg;
import io.littlehorse.sdk.wfsdk.ExternalEventNodeOutput;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.LHWorkflow;
import io.littlehorse.test.WorkflowVerifier;
import java.time.Duration;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

@LHTest
public class CorrelatedEventTest {

    private WorkflowVerifier verifier;
    private LittleHorseBlockingStub client;

    private static final ExternalEventDefId DELETION_EVT =
            ExternalEventDefId.newBuilder().setName("correlated-with-deletion").build();

    @LHWorkflow("correlated-event-with-deletion")
    public Workflow correlatedWithDeletion;

    @LHWorkflow("correlated-event-with-deletion")
    public Workflow getWfWithDeletion() {
        return Workflow.newWorkflow("correlated-event-with-deletion", wf -> {
            WfRunVariable key = wf.declareStr("key").required();
            WfRunVariable eventResult = wf.declareInt("event-result");
            ExternalEventNodeOutput output = wf.waitForEvent("correlated-with-deletion")
                    .registeredAs(Integer.class)
                    .withCorrelationId(key)
                    .withCorrelatedEventConfig(CorrelatedEventConfig.newBuilder()
                            .setDeleteAfterFirstCorrelation(true)
                            .build());
            eventResult.assign(output);
        });
    }

    @Test
    void correlatedEventShouldBeDeletedIfConfigSaysSo() {
        // Hack: just start a WfRun that we don't care about to ensure that the e2e framework
        // registers the externalEventDef. TODO (#1593): make this native.
        verifier.prepareRun(correlatedWithDeletion, Arg.of("key", "blah blah")).start();

        String key = LHUtil.generateGuid();
        CorrelatedEvent event = client.putCorrelatedEvent(PutCorrelatedEventRequest.newBuilder()
                .setContent(LHLibUtil.objToVarVal(137L))
                .setKey(key)
                .setExternalEventDefId(DELETION_EVT)
                .build());

        CorrelatedEventId eventId = event.getId();
        Assertions.assertThat(eventId)
                .isEqualTo(CorrelatedEventId.newBuilder()
                        .setExternalEventDefId(DELETION_EVT)
                        .setKey(key)
                        .build());

        Assertions.assertThat(client.getCorrelatedEvent(eventId).getContent().getInt())
                .isEqualTo(137L);

        verifier.prepareRun(correlatedWithDeletion, Arg.of("key", key))
                .waitForStatus(LHStatus.COMPLETED, Duration.ofSeconds(3))
                .thenVerifyVariable(0, "event-result", variable -> {
                    Assertions.assertThat(variable.getInt()).isEqualTo(137L);
                })
                .start();

        Assertions.assertThatThrownBy(() -> {
                    client.getCorrelatedEvent(eventId);
                })
                .matches(exn -> {
                    StatusRuntimeException sre = (StatusRuntimeException) exn;
                    return sre.getStatus().getCode() == Code.NOT_FOUND;
                });
    }

    @Test
    void shouldCompleteIfEventSentAfterWfRunStarts() {
        String key = LHUtil.generateGuid();
        verifier.prepareRun(correlatedWithDeletion, Arg.of("key", key))
                .waitForStatus(LHStatus.RUNNING)
                // TODO (#1593): make this native in the e2e framework
                .thenVerifyWfRun(wfRun -> {
                    try {
                        // The sleep is because there is a delay between the WfRun being
                        // run and the CorrelationMarker arriving at the correct partition.
                        // This is because it must go through the timer topology.
                        //
                        // We want to make sure that the marker gets there before the
                        // WfRun, hence the sleep.
                        Thread.sleep(1000);
                    } catch (Exception exn) {
                    }
                    client.putCorrelatedEvent(PutCorrelatedEventRequest.newBuilder()
                            .setContent(LHLibUtil.objToVarVal(42))
                            .setKey(key)
                            .setExternalEventDefId(DELETION_EVT)
                            .build());
                })
                .waitForStatus(LHStatus.COMPLETED, Duration.ofSeconds(3))
                .thenVerifyVariable(0, "event-result", variable -> {
                    Assertions.assertThat(variable.getInt()).isEqualTo(42L);
                })
                .start();
    }
}
