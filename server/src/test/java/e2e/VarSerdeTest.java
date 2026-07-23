package e2e;

import io.grpc.stub.StreamObserver;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.LHHostInfo;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseStub;
import io.littlehorse.sdk.common.proto.PollTaskRequest;
import io.littlehorse.sdk.common.proto.PollTaskResponse;
import io.littlehorse.sdk.common.proto.PutTaskDefRequest;
import io.littlehorse.sdk.common.proto.RegisterTaskWorkerRequest;
import io.littlehorse.sdk.common.proto.RegisterTaskWorkerResponse;
import io.littlehorse.sdk.common.proto.ReportTaskRun;
import io.littlehorse.sdk.common.proto.ScheduledTask;
import io.littlehorse.sdk.common.proto.TaskDefId;
import io.littlehorse.sdk.common.proto.TaskStatus;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.LHWorkflow;
import io.littlehorse.test.WorkflowVerifier;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

/**
 * End-to-end validation that the {@code ReportTaskRun} RPC accepts a worker-reported output and
 * correctly propagates a deserialization failure into the {@code TaskRun}.
 *
 * <p>A {@code TaskRun} only accepts a reported result once its current attempt is in
 * {@code TASK_RUNNING}, which happens after a worker <em>claims</em> the scheduled task via
 * {@code PollTask}. These tests therefore reuse the worker protocol (register to discover the hosts
 * owning the task queue, then poll to claim the task) before manually reporting a malformed output so
 * we can assert the resulting {@code TASK_OUTPUT_SERDE_ERROR}.
 */
@LHTest
public class VarSerdeTest {

    private static final String TASK_NAME = "var-serde-test-task";
    private static final String WORKER_ID = "var-serde-test-worker";

    public LittleHorseBlockingStub client;
    private LHConfig config;
    private WorkflowVerifier verifier;

    @LHWorkflow("var-serde-test")
    private Workflow varSerdeWf = Workflow.newWorkflow("var-serde-test", wf -> {
        wf.execute(TASK_NAME);
    });

    @Test
    public void shouldPropagateMalformedJsonObjThroughReportTask() {
        runReportScenario(
                VariableValue.newBuilder().setJsonObj("not-a-json").build(), TaskStatus.TASK_OUTPUT_SERDE_ERROR);
    }

    @Test
    public void shouldPropagateMalformedJsonArrThroughReportTask() {
        runReportScenario(
                VariableValue.newBuilder().setJsonArr("not-a-json").build(), TaskStatus.TASK_OUTPUT_SERDE_ERROR);
    }

    @Test
    public void shouldAcceptUtcTimestampThroughReportTask() {
        runReportScenario(
                VariableValue.newBuilder()
                        .setUtcTimestamp(LHLibUtil.fromDate(new Date()))
                        .build(),
                TaskStatus.TASK_SUCCESS);
    }

    /**
     * Registers the {@link #TASK_NAME} TaskDef, runs the workflow, claims the scheduled task via the
     * worker poll protocol, reports the given {@code output}, and asserts the resulting task status.
     */
    private void runReportScenario(VariableValue output, TaskStatus expectedStatus) {
        client.putTaskDef(PutTaskDefRequest.newBuilder().setName(TASK_NAME).build());
        Awaitility.await()
                .atMost(Duration.ofSeconds(10))
                .ignoreExceptions()
                .until(() -> client.getTaskDef(
                                TaskDefId.newBuilder().setName(TASK_NAME).build())
                        != null);

        AtomicReference<ScheduledTask> claimedTask = new AtomicReference<>();
        verifier.prepareRun(varSerdeWf)
                // Claim the scheduled task the way a worker does; this transitions the attempt to
                // TASK_RUNNING so the server will accept our reported result.
                .thenVerifyTaskRun(0, 1, taskRun -> claimedTask.set(claimNextTask()))
                .waitForTaskStatus(0, 1, TaskStatus.TASK_RUNNING)
                // Report the (possibly malformed) output as the worker would.
                .thenVerifyTaskRun(0, 1, taskRun -> reportTask(claimedTask.get(), output))
                .waitForTaskStatus(0, 1, expectedStatus)
                .start();
    }

    /** Reports a result for the claimed task, mirroring what the worker SDK's executor sends. */
    private void reportTask(ScheduledTask scheduledTask, VariableValue output) {
        client.reportTask(ReportTaskRun.newBuilder()
                .setTaskRunId(scheduledTask.getTaskRunId())
                .setAttemptNumber(scheduledTask.getAttemptNumber())
                .setTime(LHLibUtil.fromDate(new Date()))
                .setStatus(TaskStatus.TASK_SUCCESS)
                .setOutput(output)
                .build());
    }

    /**
     * Claims the next scheduled task for {@link #TASK_NAME} using the same protocol as the worker SDK:
     * register to discover which server instances own the task queue, then open a {@code PollTask}
     * stream to each and take whichever hands out the task first.
     */
    private ScheduledTask claimNextTask() {
        TaskDefId taskDefId = TaskDefId.newBuilder().setName(TASK_NAME).build();
        RegisterTaskWorkerResponse registration = client.registerTaskWorker(RegisterTaskWorkerRequest.newBuilder()
                .setTaskDefId(taskDefId)
                .setTaskWorkerId(WORKER_ID)
                .build());

        CompletableFuture<ScheduledTask> claimed = new CompletableFuture<>();
        List<StreamObserver<PollTaskRequest>> pollStreams = new ArrayList<>();
        for (LHHostInfo host : registration.getYourHostsList()) {
            LittleHorseStub asyncStub = config.getAsyncStub(host.getHost(), host.getPort());
            StreamObserver<PollTaskRequest> pollStream = asyncStub.pollTask(new StreamObserver<>() {
                @Override
                public void onNext(PollTaskResponse response) {
                    if (response.hasResult()) {
                        claimed.complete(response.getResult());
                    }
                }

                @Override
                public void onError(Throwable t) {
                    // A single host erroring shouldn't fail the claim; another host may hold the task.
                }

                @Override
                public void onCompleted() {}
            });
            pollStream.onNext(PollTaskRequest.newBuilder()
                    .setClientId(WORKER_ID)
                    .setTaskDefId(taskDefId)
                    .setTaskWorkerVersion("0")
                    .build());
            pollStreams.add(pollStream);
        }

        try {
            return claimed.get(15, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException("Failed to claim scheduled task for " + TASK_NAME, e);
        } finally {
            pollStreams.forEach(StreamObserver::onCompleted);
        }
    }
}
