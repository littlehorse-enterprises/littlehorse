package e2e;

import static org.assertj.core.api.Assertions.assertThat;

import io.littlehorse.sdk.common.proto.LHStatus;
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

    private WorkflowVerifier workflowVerifier;

    private int executionsAtLocation0;
    private int executionsInCheckpoint1;
    private int executionsAtLocation2;
    private int executionsInCheckpoint2;

    @LHWorkflow("checkpointed-task-test")
    private Workflow workflow;

    @Test
    public void checkpointedTasksTest() {
        workflowVerifier
                .prepareRun(workflow)
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

    @LHWorkflow("checkpointed-task-test")
    public Workflow buildWorkflow() {
        return new WorkflowImpl("checkpointed-task-test", thread -> {
            thread.complete(thread.execute("task-with-checkpoint").withRetries(1));
        });
    }

    @LHTaskMethod("task-with-checkpoint")
    public String obiWan(WorkerContext context) {
        int attemptNumber = context.getAttemptNumber();

        executionsAtLocation0++;

        String result = context.executeAndCheckpoint(
                () -> {
                    executionsInCheckpoint1++;
                    return "hello from first checkpoint";
                },
                String.class);

        executionsAtLocation2++;

        if (attemptNumber == 0) {
            throw new RuntimeException("Throwing a failure in the second checkpoint to show how the checkpoint works");
        }

        result += context.executeAndCheckpoint(
                () -> {
                    executionsInCheckpoint2++;
                    return " and the second checkpoint";
                },
                String.class);

        return result + " and after the second checkpoint";
    }
}
