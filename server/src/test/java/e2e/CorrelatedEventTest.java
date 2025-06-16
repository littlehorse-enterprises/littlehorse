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
import io.littlehorse.sdk.wfsdk.WorkflowThread;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.LHWorkflow;
import io.littlehorse.test.WorkflowVerifier;
import java.time.Duration;
import java.util.List;
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

    @LHWorkflow("correlated-event-no-deletion")
    public Workflow correlatedNoDeletion;

    @LHWorkflow("multi-thread-correlation")
    public Workflow multiThreadCorrelation;

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

    @LHWorkflow("correlated-event-no-deletion")
    public Workflow getWfWithoutDeletion() {
        return Workflow.newWorkflow("correlated-event-no-deletion", wf -> {
            WfRunVariable key = wf.declareStr("key").required();
            WfRunVariable eventResult = wf.declareInt("event-result");
            ExternalEventNodeOutput output = wf.waitForEvent("correlated-with-deletion")
                    .registeredAs(Integer.class)
                    .withCorrelationId(key)
                    .withCorrelatedEventConfig(CorrelatedEventConfig.newBuilder()
                            .setDeleteAfterFirstCorrelation(false)
                            .build());
            eventResult.assign(output);
        });
    }

    @LHWorkflow("multi-thread-correlation")
    public Workflow getThreadWf() {
        return Workflow.newWorkflow("multi-thread-correlation", wf -> {
            WfRunVariable documents = wf.declareJsonArr("documents").required();

            var threads = wf.spawnThreadForEach(documents, "wait-for-sign", child -> {
                WfRunVariable documentId = child.declareStr(WorkflowThread.HANDLER_INPUT_VAR);
                child.waitForEvent("correlated-document-signed")
                        .registeredAs(null)
                        .withCorrelationId(documentId);
            });
            wf.waitForThreads(threads);
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
    void correlatedEventShouldNotBeDeletedIfConfigSaysSo() {
        // Hack: just start a WfRun that we don't care about to ensure that the e2e framework
        // registers the externalEventDef. TODO (#1593): make this native.
        verifier.prepareRun(correlatedNoDeletion, Arg.of("key", "blah blah")).start();

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

        Assertions.assertThat(client.getCorrelatedEvent(eventId).getExternalEventsCount())
                .isEqualTo(1);
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

    @Test
    void correlatedEventsGoToCorrectNodeRun() {
        String randomStr = LHUtil.generateGuid(); // avoid ALREADY_EXISTS on repeated test runs
        List<String> documents = List.of("doc-1" + randomStr, "doc-2" + randomStr, "doc-3" + randomStr);
        ExternalEventDefId evtId = ExternalEventDefId.newBuilder()
                .setName("correlated-document-signed")
                .build();
        verifier.prepareRun(multiThreadCorrelation, Arg.of("documents", documents))
                .thenVerifyWfRun(wfRun -> {
                    client.putCorrelatedEvent(PutCorrelatedEventRequest.newBuilder()
                            .setExternalEventDefId(evtId)
                            .setKey("doc-2" + randomStr)
                            .build());
                })
                .waitForThreadRunStatus(2, LHStatus.COMPLETED)
                .waitForThreadRunStatus(0, LHStatus.RUNNING)
                .waitForThreadRunStatus(1, LHStatus.RUNNING)
                .thenVerifyWfRun(wfRun -> {
                    client.putCorrelatedEvent(PutCorrelatedEventRequest.newBuilder()
                            .setExternalEventDefId(evtId)
                            .setKey("doc-1" + randomStr)
                            .build());
                })
                .waitForThreadRunStatus(1, LHStatus.COMPLETED)
                .thenVerifyWfRun(wfRun -> {
                    client.putCorrelatedEvent(PutCorrelatedEventRequest.newBuilder()
                            .setExternalEventDefId(evtId)
                            .setKey("doc-3" + randomStr)
                            .build());
                })
                .waitForStatus(LHStatus.COMPLETED)
                .start();
    }
}
