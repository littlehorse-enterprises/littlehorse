package e2e;

import com.google.protobuf.Timestamp;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.proto.DeleteWfRunRequest;
import io.littlehorse.sdk.common.proto.ExternalEventDefId;
import io.littlehorse.sdk.common.proto.ExternalEventId;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.ListNodeRunsRequest;
import io.littlehorse.sdk.common.proto.ListTaskRunsRequest;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.NodeRun;
import io.littlehorse.sdk.common.proto.PutExternalEventDefRequest;
import io.littlehorse.sdk.common.proto.PutExternalEventRequest;
import io.littlehorse.sdk.common.proto.SearchWfRunRequest;
import io.littlehorse.sdk.common.proto.TaskRunId;
import io.littlehorse.sdk.common.proto.WfRunId;
import io.littlehorse.sdk.common.proto.WfRunIdList;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.LHWorkflow;
import io.littlehorse.test.WorkflowVerifier;
import io.littlehorse.test.exception.LHTestExceptionUtil;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@LHTest(externalEventNames = {WfRunDeletionTest.COMPLETE_WFRUN_EVT, WfRunDeletionTest.IGNORED_EVT})
public class WfRunDeletionTest {

    public static final String COMPLETE_WFRUN_EVT = "pedro-delete-wfrun-complete";
    public static final String IGNORED_EVT = "pedro-delete-wfrun-ignored";

    @LHWorkflow("delete-wfrun")
    public Workflow basicExternalEvent;

    private LittleHorseBlockingStub client;
    private WorkflowVerifier verifier;

    @LHWorkflow("delete-wfrun")
    public Workflow getBasicExternalEventWorkflow() {
        return Workflow.newWorkflow("delete-wfrun", wf -> {
            wf.waitForEvent(COMPLETE_WFRUN_EVT);
            wf.execute("pedro-task-test");
        });
    }

    @Test
    @Order(1)
    void deletingWfRunShouldDeleteIndexEntries() {
        WfRunId id = WfRunId.newBuilder().setId(LHUtil.generateGuid()).build();
        verifier.prepareRun(basicExternalEvent)
                .thenSendExternalEventWithContent(COMPLETE_WFRUN_EVT, "asdf")
                .waitForStatus(LHStatus.COMPLETED)
                .start(id);

        // Make sure that we can find the wfRun in a search
        Timestamp oneMinuteAgo = LHLibUtil.fromDate(
                new Date(Instant.now().minus(Duration.ofMinutes(1)).toEpochMilli()));
        WfRunIdList searchResult = client.searchWfRun(SearchWfRunRequest.newBuilder()
                .setWfSpecName("delete-wfrun")
                .setEarliestStart(oneMinuteAgo)
                .build());

        assertThat(searchResult.getResultsList().stream()
                .anyMatch(candidate -> candidate.getId().equals(id.getId()))).isTrue();

        // Delete the WfRun
        client.deleteWfRun(DeleteWfRunRequest.newBuilder().setId(id).build());

        // Make sure that we can't find the wfRun in a search
        searchResult = client.searchWfRun(SearchWfRunRequest.newBuilder()
                .setWfSpecName("delete-wfrun")
                .setEarliestStart(oneMinuteAgo)
                .build());
        assertThat(!searchResult.getResultsList().stream()
                .anyMatch(candidate -> candidate.getId().equals(id.getId()))).isTrue();

        // Obviously we shouldn't be able to find the WfRun
        assertThat(LHTestExceptionUtil.throwsNotFound(() -> {
            client.getWfRun(id);
            return null;
        })).isTrue();
    }

    @Test
    @Order(2)
    void deletingWfRunShouldDeleteNodeRuns() {
        WfRunId id = WfRunId.newBuilder().setId(LHUtil.generateGuid()).build();
        verifier.prepareRun(basicExternalEvent)
                .thenSendExternalEventWithContent(COMPLETE_WFRUN_EVT, "asdf")
                .waitForStatus(LHStatus.COMPLETED)
                .start(id);

        // Find the TaskRun
        List<NodeRun> nodeRuns = client.listNodeRuns(
                        ListNodeRunsRequest.newBuilder().setWfRunId(id).build())
                .getResultsList();

        // Delete the WfRun
        client.deleteWfRun(DeleteWfRunRequest.newBuilder().setId(id).build());

        // Verify that all of the NodeRuns are gone;
        for (NodeRun nodeRun : nodeRuns) {
            assertThat(LHTestExceptionUtil.throwsNotFound(() -> {
                client.getNodeRun(nodeRun.getId());
                return null;
            })).isTrue();
        }
    }

    @Test
    @Order(3)
    void deletingWfRunShouldDeleteExternalEvents() {
        WfRunId id = WfRunId.newBuilder().setId(LHUtil.generateGuid()).build();
        String firstEvtGuid = "event-from-before-wfrun";
        String secondEvtGuid = "event-from-during-wfrun";

        // The test util doesn't create events not used by the wfspec
        ExternalEventDefId ignoredEvtDefId = client.putExternalEventDef(PutExternalEventDefRequest.newBuilder()
                        .setName(IGNORED_EVT)
                        .build())
                .getId();

        ExternalEventId firstEvtId = ExternalEventId.newBuilder()
                .setWfRunId(id)
                .setExternalEventDefId(ignoredEvtDefId)
                .setGuid(firstEvtGuid)
                .build();
        ExternalEventId secondEvtId = ExternalEventId.newBuilder()
                .setWfRunId(id)
                .setExternalEventDefId(ignoredEvtDefId)
                .setGuid(secondEvtGuid)
                .build();

        client.putExternalEvent(PutExternalEventRequest.newBuilder()
                .setContent(LHLibUtil.objToVarVal("hello there"))
                .setWfRunId(id)
                .setExternalEventDefId(ExternalEventDefId.newBuilder().setName(IGNORED_EVT))
                .setGuid(firstEvtGuid)
                .build());

        verifier.prepareRun(basicExternalEvent)
                .thenVerifyWfRun((wfRun) -> {
                    // Need to set specific guid for event for testing
                    client.putExternalEvent(PutExternalEventRequest.newBuilder()
                            .setContent(LHLibUtil.objToVarVal("hello there"))
                            .setWfRunId(id)
                            .setExternalEventDefId(
                                    ExternalEventDefId.newBuilder().setName(IGNORED_EVT))
                            .setGuid(secondEvtGuid)
                            .build());
                })
                .thenSendExternalEventWithContent(COMPLETE_WFRUN_EVT, "something")
                .waitForStatus(LHStatus.COMPLETED)
                .start(id);

        // Verify that the ExternalEvents are there. This will throw a StatusRuntimeException if not.
        client.getExternalEvent(firstEvtId);
        client.getExternalEvent(secondEvtId);

        // Delete the WfRun
        client.deleteWfRun(DeleteWfRunRequest.newBuilder().setId(id).build());

        // Verify that the ExternalEvents are gone.
        assertThat(LHTestExceptionUtil.throwsNotFound(() -> {
            client.getExternalEvent(firstEvtId);
            return null;
        })).isTrue();

        assertThat(LHTestExceptionUtil.throwsNotFound(() -> {
            client.getExternalEvent(secondEvtId);
            return null;
        })).isTrue();
    }

    @Test
    void deletingWfRunShouldDeleteTaskRuns() {
        WfRunId id = WfRunId.newBuilder().setId(LHUtil.generateGuid()).build();
        verifier.prepareRun(basicExternalEvent)
                .thenSendExternalEventWithContent(COMPLETE_WFRUN_EVT, "asdf")
                .waitForStatus(LHStatus.COMPLETED)
                .start(id);

        // Find the TaskRun
        TaskRunId taskRunId = client.listTaskRuns(
                        ListTaskRunsRequest.newBuilder().setWfRunId(id).build())
                .getResults(0)
                .getId();

        // Delete the WfRun
        client.deleteWfRun(DeleteWfRunRequest.newBuilder().setId(id).build());

        // Verify that the TaskRun is gone.
        assertThat(LHTestExceptionUtil.throwsNotFound(() -> {
            client.getTaskRun(taskRunId);
            return null;
        })).isTrue();
    }

    @LHTaskMethod("pedro-task-test")
    public String pedro() {
        return "Pascal";
    }
}
