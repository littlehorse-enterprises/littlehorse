package e2e;

import static org.assertj.core.api.Assertions.*;

import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.proto.BulkDeleteWfRun;
import io.littlehorse.sdk.common.proto.BulkJob;
import io.littlehorse.sdk.common.proto.CreateBulkJobRequest;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc;
import io.littlehorse.sdk.common.proto.SearchWfRunRequest;
import io.littlehorse.sdk.common.proto.WfRunId;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.LHWorkflow;
import io.littlehorse.test.WorkflowVerifier;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@LHTest
@Tag("slow")
public class WfRunBulkDeletion {

    private LittleHorseGrpc.LittleHorseBlockingStub client;
    private WorkflowVerifier verifier;

    @LHWorkflow("instant-complete")
    public Workflow instantComplete;

    @LHWorkflow("infinite-running")
    public Workflow infiniteRunning;

    private void runWfRuns(Workflow workflow, LHStatus waitForStatus, int numRuns) {
        for (int i = 0; i < numRuns; i++) {
            verifier.prepareRun(workflow).waitForStatus(waitForStatus).start();
        }
    }

    private List<WfRunId> search(Workflow workflow, LHStatus expectedStatus) {
        return client.searchWfRun(SearchWfRunRequest.newBuilder()
                        .setWfSpecName(workflow.getName())
                        .setStatus(expectedStatus)
                        .build())
                .getResultsList();
    }

    public BulkJob delete(Workflow workflow, LHStatus targetStatus) {
        BulkDeleteWfRun.Builder bulkDeleteWfRun = BulkDeleteWfRun.newBuilder();
        bulkDeleteWfRun.setWfSpecName(workflow.getName());
        bulkDeleteWfRun.setWfRunStatus(targetStatus);
        bulkDeleteWfRun.setEarliestStart(LHLibUtil.fromDate(new Date(System.currentTimeMillis() - 10000)));
        bulkDeleteWfRun.setLatestStart(LHLibUtil.fromDate(new Date()));
        return client.createBulkJob(CreateBulkJobRequest.newBuilder()
                .setBulkDeleteWfRun(bulkDeleteWfRun)
                .build());
    }

    @Test
    public void shouldDeleteCompletedWfRuns() throws InterruptedException {
        runWfRuns(instantComplete, LHStatus.COMPLETED, 3);
        runWfRuns(infiniteRunning, LHStatus.RUNNING, 3);
        List<WfRunId> idsBeforeDeletion = search(instantComplete, LHStatus.COMPLETED);
        assertThat(idsBeforeDeletion).hasSize(3);
        delete(instantComplete, LHStatus.COMPLETED);
        // Wait for bulkjob to complete
        TimeUnit.SECONDS.sleep(30);
        List<WfRunId> idsAfterDeletion = search(instantComplete, LHStatus.COMPLETED);
        assertThat(idsAfterDeletion).isEmpty();
        List<WfRunId> infiniteRunningIds = search(infiniteRunning, LHStatus.RUNNING);
        assertThat(infiniteRunningIds).hasSize(3);
    }

    @LHWorkflow("instant-complete")
    public Workflow getInstantComplete() {
        return Workflow.newWorkflow("instant-complete", wf -> {});
    }

    @LHWorkflow("infinite-running")
    public Workflow getInfiniteRunning() {
        return Workflow.newWorkflow("infinite-running", wf -> wf.sleepSeconds(2_000_000_000L));
    }
}
