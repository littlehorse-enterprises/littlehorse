package e2e;

import static org.assertj.core.api.Assertions.assertThat;

import io.littlehorse.sdk.common.proto.InactiveThreadRun;
import io.littlehorse.sdk.common.proto.InactiveThreadRunId;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.ThreadRetentionPolicy;
import io.littlehorse.sdk.common.proto.ThreadType;
import io.littlehorse.sdk.common.proto.VariableMutationType;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.wfsdk.SpawnedThread;
import io.littlehorse.sdk.wfsdk.SpawnedThreads;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.LHWorkflow;
import io.littlehorse.test.WorkflowVerifier;
import java.util.Map;
import org.junit.jupiter.api.Test;

@LHTest(externalEventNames = {InactiveThreadRunArchivalTest.FINISH_WORKFLOW_EVENT})
public class InactiveThreadRunArchivalTest {

    public static final String FINISH_WORKFLOW_EVENT = "finish-archival-workflow";

    private WorkflowVerifier verifier;
    private LittleHorseBlockingStub lhClient;

    @LHWorkflow("inactive-thread-run-archival-workflow")
    private Workflow inactiveThreadRunArchivalWorkflow;

    @Test
    void shouldArchiveCompletedChildThreadRuns() {
        verifier.prepareRun(inactiveThreadRunArchivalWorkflow)
                .waitForStatus(LHStatus.RUNNING)
                .thenVerifyWfRun(wfRun -> {
                    assertThat(wfRun.getThreadRunsCount()).isEqualTo(1);
                    assertThat(wfRun.getThreadRuns(0).getNumber()).isEqualTo(0);
                    assertThat(wfRun.getGreatestThreadrunNumber()).isEqualTo(2);
                })
                .thenVerifyWfRun(wfRun -> {
                    InactiveThreadRun firstArchived = lhClient.getInactiveThreadRun(InactiveThreadRunId.newBuilder()
                            .setWfRunId(wfRun.getId())
                            .setThreadRunNumber(1)
                            .build());
                    InactiveThreadRun secondArchived = lhClient.getInactiveThreadRun(InactiveThreadRunId.newBuilder()
                            .setWfRunId(wfRun.getId())
                            .setThreadRunNumber(2)
                            .build());

                    assertThat(firstArchived.getThreadRun().getStatus()).isEqualTo(LHStatus.COMPLETED);
                    assertThat(firstArchived.getThreadRun().getType()).isEqualTo(ThreadType.CHILD);
                    assertThat(secondArchived.getThreadRun().getStatus()).isEqualTo(LHStatus.COMPLETED);
                    assertThat(secondArchived.getThreadRun().getType()).isEqualTo(ThreadType.CHILD);
                })
                .thenSendExternalEventWithContent(FINISH_WORKFLOW_EVENT, Map.of())
                .waitForStatus(LHStatus.COMPLETED)
                .start();
    }

    @LHWorkflow("inactive-thread-run-archival-workflow")
    public Workflow buildInactiveThreadRunArchivalWorkflow() {
        return Workflow.newWorkflow("inactive-thread-run-archival-workflow", wf -> {
            SpawnedThread child1 = wf.spawnThread(
                    child -> {
                        child.withRetentionPolicy(ThreadRetentionPolicy.newBuilder()
                                .setSecondsAfterThreadTermination(0)
                                .build());
                        WfRunVariable done = child.addVariable("done-1", VariableType.BOOL);
                        child.mutate(done, VariableMutationType.ASSIGN, true);
                    },
                    "child-1",
                    null);
            SpawnedThread child2 = wf.spawnThread(
                    child -> {
                        child.withRetentionPolicy(ThreadRetentionPolicy.newBuilder()
                                .setSecondsAfterThreadTermination(0)
                                .build());
                        WfRunVariable done = child.addVariable("done-2", VariableType.BOOL);
                        child.mutate(done, VariableMutationType.ASSIGN, true);
                    },
                    "child-2",
                    null);

            wf.waitForThreads(SpawnedThreads.of(child1, child2));
            wf.waitForEvent(FINISH_WORKFLOW_EVENT);
        });
    }
}
