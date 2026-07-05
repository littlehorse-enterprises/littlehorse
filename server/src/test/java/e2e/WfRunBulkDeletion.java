package e2e;

import static org.assertj.core.api.Assertions.*;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.proto.BulkDeleteWfRun;
import io.littlehorse.sdk.common.proto.BulkJob;
import io.littlehorse.sdk.common.proto.BulkJobId;
import io.littlehorse.sdk.common.proto.BulkJobStatus;
import io.littlehorse.sdk.common.proto.CreateBulkJobRequest;
import io.littlehorse.sdk.common.proto.DeleteBulkJobRequest;
import io.littlehorse.sdk.common.proto.GetBulkJobRequest;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc;
import io.littlehorse.sdk.common.proto.SearchBulkJobRequest;
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

    @Test
    public void shouldSearchByStatusThenDeleteCompletedBulkJob() throws InterruptedException {
        runWfRuns(instantComplete, LHStatus.COMPLETED, 2);

        BulkJobId jobId = delete(instantComplete, LHStatus.COMPLETED).getId();
        BulkJob completed = waitForBulkJobCompletion(jobId);
        assertThat(completed.getStatus()).isEqualTo(BulkJobStatus.BULK_JOB_COMPLETED);

        // The completed job must show up when filtering by BULK_JOB_COMPLETED.
        assertThat(searchBulkJobs(BulkJobStatus.BULK_JOB_COMPLETED)).contains(jobId);

        // Deleting a finished BulkJob removes it from both Get and Search.
        client.deleteBulkJob(
                DeleteBulkJobRequest.newBuilder().setId(jobId).build());

        assertThatThrownBy(() -> client.getBulkJob(
                        GetBulkJobRequest.newBuilder().setId(jobId).build()))
                .isInstanceOf(StatusRuntimeException.class)
                .extracting(exn -> ((StatusRuntimeException) exn).getStatus().getCode())
                .isEqualTo(Status.Code.NOT_FOUND);

        assertThat(searchBulkJobs(BulkJobStatus.BULK_JOB_COMPLETED)).doesNotContain(jobId);
    }

    private List<BulkJobId> searchBulkJobs(BulkJobStatus status) {
        return client.searchBulkJob(
                        SearchBulkJobRequest.newBuilder().setStatus(status).build())
                .getResultsList();
    }

    private BulkJob waitForBulkJobCompletion(BulkJobId jobId) throws InterruptedException {
        // The punctuator runs on a 1s schedule; poll until the job reaches a terminal state.
        for (int i = 0; i < 40; i++) {
            BulkJob job = client.getBulkJob(
                    GetBulkJobRequest.newBuilder().setId(jobId).build());
            if (job.getStatus() != BulkJobStatus.BULK_JOB_RUNNING) {
                return job;
            }
            TimeUnit.SECONDS.sleep(1);
        }
        throw new AssertionError("BulkJob %s did not complete in time".formatted(jobId.getId()));
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
