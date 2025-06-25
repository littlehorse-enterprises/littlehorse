package e2e;

import io.grpc.Status.Code;
import io.grpc.StatusRuntimeException;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.proto.CorrelatedEvent;
import io.littlehorse.sdk.common.proto.CorrelatedEventConfig;
import io.littlehorse.sdk.common.proto.CorrelatedEventId;
import io.littlehorse.sdk.common.proto.DeleteWfRunRequest;
import io.littlehorse.sdk.common.proto.ExternalEventDefId;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.PutCorrelatedEventRequest;
import io.littlehorse.sdk.common.proto.StopWfRunRequest;
import io.littlehorse.sdk.common.proto.WfRunId;
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

    private static final ExternalEventDefId NO_DELETION_EVT =
            ExternalEventDefId.newBuilder().setName("correlated-no-deletion").build();

    @LHWorkflow("correlated-event-with-deletion")
    public Workflow correlatedWithDeletionAndMask;

    @LHWorkflow("correlated-event-no-deletion")
    public Workflow correlatedNoDeletionAndNoMask;

    @LHWorkflow("multi-thread-correlation")
    public Workflow multiThreadCorrelation;

    @LHWorkflow("correlated-event-with-deletion")
    public Workflow getWfWithDeletion() {
        return Workflow.newWorkflow("correlated-event-with-deletion", wf -> {
            WfRunVariable key = wf.declareStr("key").required().masked();
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
            ExternalEventNodeOutput output = wf.waitForEvent("correlated-no-deletion")
                    .registeredAs(Integer.class)
                    .withCorrelationId(key)
                    .timeout(1)
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
        verifier.prepareRun(correlatedWithDeletionAndMask, Arg.of("key", "blah blah"))
                .start();

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

        verifier.prepareRun(correlatedWithDeletionAndMask, Arg.of("key", key))
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
        verifier.prepareRun(correlatedNoDeletionAndNoMask, Arg.of("key", "blah blah"))
                .start();

        String key = LHUtil.generateGuid();
        CorrelatedEvent event = client.putCorrelatedEvent(PutCorrelatedEventRequest.newBuilder()
                .setContent(LHLibUtil.objToVarVal(137L))
                .setKey(key)
                .setExternalEventDefId(NO_DELETION_EVT)
                .build());

        CorrelatedEventId eventId = event.getId();
        Assertions.assertThat(eventId)
                .isEqualTo(CorrelatedEventId.newBuilder()
                        .setExternalEventDefId(NO_DELETION_EVT)
                        .setKey(key)
                        .build());

        Assertions.assertThat(client.getCorrelatedEvent(eventId).getContent().getInt())
                .isEqualTo(137L);

        verifier.prepareRun(correlatedNoDeletionAndNoMask, Arg.of("key", key))
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
        verifier.prepareRun(correlatedWithDeletionAndMask, Arg.of("key", key))
                .waitForStatus(LHStatus.RUNNING)
                .thenSendCorrelatedEvent(DELETION_EVT.getName(), key, 42)
                .waitForStatus(LHStatus.COMPLETED, Duration.ofSeconds(3))
                .thenVerifyVariable(0, "event-result", variable -> {
                    Assertions.assertThat(variable.getInt()).isEqualTo(42L);
                })
                .start();
    }

    @Test
    void shouldNotShowMaskedKey() {
        String key = LHUtil.generateGuid();
        verifier.prepareRun(correlatedWithDeletionAndMask, Arg.of("key", key))
                .waitForStatus(LHStatus.RUNNING)
                .thenSendCorrelatedEvent(DELETION_EVT.getName(), key, 42)
                .waitForStatus(LHStatus.COMPLETED, Duration.ofSeconds(3))
                .thenVerifyNodeRun(0, 1, nodeRun -> {
                    Assertions.assertThat(nodeRun.getExternalEvent().getCorrelationKey())
                            .contains("***");
                })
                .start();
    }

    @Test
    void shouldShowUnmaskedKey() {
        String key = LHUtil.generateGuid();
        verifier.prepareRun(correlatedNoDeletionAndNoMask, Arg.of("key", key))
                .waitForStatus(LHStatus.RUNNING)
                .thenSendCorrelatedEvent(NO_DELETION_EVT.getName(), key, 42)
                .waitForStatus(LHStatus.COMPLETED, Duration.ofSeconds(3))
                .thenVerifyNodeRun(0, 1, nodeRun -> {
                    Assertions.assertThat(nodeRun.getExternalEvent().getCorrelationKey())
                            .isEqualTo(key);
                })
                .start();
    }

    @Test
    void correlatedEventsGoToCorrectNodeRun() {
        String randomStr = LHUtil.generateGuid(); // avoid ALREADY_EXISTS on repeated test runs
        List<String> documents = List.of("doc-1", "doc-2", "doc-3");
        ExternalEventDefId evtId = ExternalEventDefId.newBuilder()
                .setName("correlated-document-signed")
                .build();
        verifier.prepareRun(multiThreadCorrelation, Arg.of("documents", documents))
                .thenSendCorrelatedEvent(evtId.getName(), "doc-2" + randomStr, null)
                .thenVerifyWfRun(wfRun -> {
                    client.putCorrelatedEvent(PutCorrelatedEventRequest.newBuilder()
                            .setExternalEventDefId(evtId)
                            .setKey("doc-2")
                            .build());
                })
                .waitForThreadRunStatus(2, LHStatus.COMPLETED)
                .waitForThreadRunStatus(0, LHStatus.RUNNING)
                .waitForThreadRunStatus(1, LHStatus.RUNNING)
                .thenSendCorrelatedEvent(evtId.getName(), "doc-1" + randomStr, null)
                .thenVerifyWfRun(wfRun -> {
                    client.putCorrelatedEvent(PutCorrelatedEventRequest.newBuilder()
                            .setExternalEventDefId(evtId)
                            .setKey("doc-1")
                            .build());
                })
                .waitForThreadRunStatus(1, LHStatus.COMPLETED)
                .thenVerifyWfRun(wfRun -> {
                    client.putCorrelatedEvent(PutCorrelatedEventRequest.newBuilder()
                            .setExternalEventDefId(evtId)
                            .setKey("doc-3")
                            .build());
                })
                .thenSendCorrelatedEvent(evtId.getName(), "doc-3" + randomStr, null)
                .waitForStatus(LHStatus.COMPLETED)
                .start();
    }

    @Test
    void shouldDeleteCorrelationMarkerWhenWfRunDeleted() throws InterruptedException {
        String randomStr = LHUtil.generateGuid();
        WfRunId wfRunId = verifier.prepareRun(correlatedNoDeletionAndNoMask, Arg.of("key", randomStr))
                .waitForStatus(LHStatus.RUNNING)
                .start();

        client.stopWfRun(StopWfRunRequest.newBuilder().setWfRunId(wfRunId).build());
        client.deleteWfRun(DeleteWfRunRequest.newBuilder().setId(wfRunId).build());

        // Sleep a little...there's unfortunately no way to look for the Correlation Marker,
        // so we just hope that the timer has boomeranged in time. Because we can't look for
        // the CorrelationMarker in the public API, we can't use Awaitility.
        //
        // If this test becomes flaky we can remove the test and replace with unit tests.
        Thread.sleep(500);

        // Now put a CorrelatedEvent, and ensure that there is no ExternalEvent created.
        CorrelatedEvent result = client.putCorrelatedEvent(PutCorrelatedEventRequest.newBuilder()
                .setKey(randomStr)
                .setContent(LHLibUtil.objToVarVal(0))
                .setExternalEventDefId(NO_DELETION_EVT)
                .build());

        // If the WfRun cleanup worked, then we don't have any events created.
        Assertions.assertThat(result.getExternalEventsCount()).isZero();
    }

    @Test
    void shouldDeleteCorrelationMarkerWhenNodeRunTimesOut() throws InterruptedException {
        String randomStr = LHUtil.generateGuid();
        verifier.prepareRun(correlatedNoDeletionAndNoMask, Arg.of("key", randomStr))
                // wait for timeout
                .waitForStatus(LHStatus.ERROR, Duration.ofSeconds(3))
                .start();

        // Sleep a little...there's unfortunately no way to look for the Correlation Marker,
        // so we just hope that the timer has boomeranged in time.
        //
        // If this test becomes flaky we can remove the test and replace with unit tests.
        Thread.sleep(500);

        // Now put a CorrelatedEvent, and ensure that there is no ExternalEvent created.
        CorrelatedEvent result = client.putCorrelatedEvent(PutCorrelatedEventRequest.newBuilder()
                .setKey(randomStr)
                .setContent(LHLibUtil.objToVarVal(0))
                .setExternalEventDefId(NO_DELETION_EVT)
                .build());

        // If the WfRun cleanup worked, then we don't have any events created.
        Assertions.assertThat(result.getExternalEventsCount()).isZero();
    }
}
