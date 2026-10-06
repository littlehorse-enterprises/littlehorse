package e2e;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

import io.grpc.StatusRuntimeException;
import io.littlehorse.sdk.common.proto.CheckpointId;
import io.littlehorse.sdk.common.proto.DeleteWfRunRequest;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.ListTaskRunsRequest;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.TaskRun;
import io.littlehorse.sdk.common.proto.TaskRunId;
import io.littlehorse.sdk.common.proto.WfRunId;
import io.littlehorse.sdk.common.util.Arg;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.sdk.worker.WorkerContext;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.LHWorkflow;
import io.littlehorse.test.WorkflowVerifier;
import org.junit.jupiter.api.Test;

@LHTest
public class CheckpointedTaskTest {

    private LittleHorseBlockingStub client;
    private WorkflowVerifier workflowVerifier;

    private int executionsAtLocation0;
    private int executionsInCheckpoint1;
    private int executionsAtLocation2;
    private int executionsInCheckpoint2;

    @LHWorkflow("checkpointed-task-test")
    private Workflow workflow;

    @LHWorkflow("checkpoint-extends-timeout-test")
    private Workflow checkpointExtendsTimeoutWorkflow;

    @Test
    public void checkpointedTasksTest() {
        workflowVerifier
                .prepareRun(workflow, Arg.of("update-executions", true))
                .waitForStatus(LHStatus.COMPLETED)
                .thenVerifyWfRun(wfRun -> {
                    assertThat(wfRun.getThreadRuns(0).getOutput().getStr())
                            .isEqualTo(
                                    "hello from first checkpoint and the second checkpoint and after the second checkpoint");
                })
                .start();

        assertThat(executionsAtLocation0).isEqualTo(2);
        assertThat(executionsInCheckpoint1).isEqualTo(1);
        assertThat(executionsAtLocation2).isEqualTo(2);
        assertThat(executionsInCheckpoint2).isEqualTo(1);
    }

    @Test
    public void shouldDeleteCheckpoints() {
        WfRunId result = workflowVerifier
                .prepareRun(workflow, Arg.of("update-executions", false))
                .waitForStatus(LHStatus.COMPLETED)
                .start();

        TaskRunId taskRun = client.listTaskRuns(
                        ListTaskRunsRequest.newBuilder().setWfRunId(result).build())
                .getResults(0)
                .getId();
        CheckpointId checkpointId = CheckpointId.newBuilder()
                .setTaskRun(taskRun)
                .setCheckpointNumber(0)
                .build();

        client.getCheckpoint(checkpointId);
        client.deleteWfRun(DeleteWfRunRequest.newBuilder().setId(result).build());

        // Checkopint should be gone
        assertThrows(StatusRuntimeException.class, () -> {
            client.getCheckpoint(checkpointId);
        });
    }

    @Test
    public void checkpointsShouldExtendTheTaskTimeout() {
        WfRunId result = workflowVerifier
                .prepareRun(checkpointExtendsTimeoutWorkflow)
                .waitForStatus(LHStatus.COMPLETED)
                .start();

        TaskRun taskRun = client.listTaskRuns(
                        ListTaskRunsRequest.newBuilder().setWfRunId(result).build())
                .getResults(0);
        assertThat(taskRun.getAttemptsCount()).isEqualTo(1);
    }

    @LHWorkflow("checkpoint-extends-timeout-test")
    public Workflow buildCheckpointExtendsTimeoutWorkflow() {
        return new WorkflowImpl("checkpoint-extends-timeout-test", thread -> {
            thread.execute("task-that-outlives-its-timeout").timeout(2);
        });
    }

    @LHTaskMethod("task-that-outlives-its-timeout")
    public String outliveTimeout(WorkerContext context) throws InterruptedException {
        for (int i = 0; i < 4; i++) {
            Thread.sleep(750);
            context.executeAndCheckpoint(checkpoint -> "still working", String.class);
        }
        return "done";
    }

    @LHWorkflow("checkpointed-task-test")
    public Workflow buildWorkflow() {
        return new WorkflowImpl("checkpointed-task-test", thread -> {
            WfRunVariable updateExecutions = thread.declareBool("update-executions");
            thread.complete(
                    thread.execute("task-with-checkpoint", updateExecutions).withRetries(1));
        });
    }

    @LHTaskMethod("task-with-checkpoint")
    public String obiWan(boolean updateExecutions, WorkerContext context) {
        int attemptNumber = context.getAttemptNumber();

        if (updateExecutions) executionsAtLocation0++;

        String result = context.executeAndCheckpoint(
                checkpoint -> {
                    if (updateExecutions) executionsInCheckpoint1++;
                    return "hello from first checkpoint";
                },
                String.class);

        if (updateExecutions) executionsAtLocation2++;

        if (attemptNumber == 0) {
            throw new RuntimeException("Throwing a failure in the second checkpoint to show how the checkpoint works");
        }

        result += context.executeAndCheckpoint(
                checkpoint -> {
                    if (updateExecutions) executionsInCheckpoint2++;
                    return " and the second checkpoint";
                },
                String.class);

        return result + " and after the second checkpoint";
    }
}
