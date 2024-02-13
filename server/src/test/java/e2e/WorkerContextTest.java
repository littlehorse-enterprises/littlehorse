package e2e;

import static org.assertj.core.api.Assertions.assertThat;

import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.TaskStatus;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.sdk.worker.WorkerContext;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.LHWorkflow;
import io.littlehorse.test.WorkflowVerifier;
import org.junit.jupiter.api.Test;

@LHTest
public class WorkerContextTest {

    private WorkflowVerifier workflowVerifier;

    @LHWorkflow("worker-context-test")
    private Workflow workflow;

    @Test
    public void simpleSequentialWorkflowExecution() {
        workflowVerifier
                .prepareRun(workflow)
                .waitForStatus(LHStatus.COMPLETED)
                .waitForTaskStatus(0, 1, TaskStatus.TASK_SUCCESS)
                .waitForTaskStatus(0, 2, TaskStatus.TASK_SUCCESS)
                .thenVerifyAllTaskRuns(0, taskRuns -> {
                    assertThat(taskRuns.get(0).getAttempts(0).getOutput().getStr())
                            .contains(" on nodeRun 1");
                    assertThat(taskRuns.get(1).getAttempts(0).getOutput().getStr())
                            .contains(" on nodeRun 2");
                })
                .start();
    }

    @Test
    public void readYourOwnWritesTest() {

        // waitForTaskStatus calls
        for (int i = 0; i < 10; i++) {
            workflowVerifier
                    .prepareRun(workflow)
                    // This step will throw a NOT_FOUND exception if the nodeRun is
                    // not returned.
                    .thenVerifyNodeRun(0, 1, nodeRun -> {})
                    .start();
        }
    }

    @LHWorkflow("worker-context-test")
    public Workflow buildWorkflow() {
        return new WorkflowImpl("worker-context-test", thread -> {
            thread.execute("worker-context-test");
            thread.execute("worker-context-test");
        });
    }

    @LHTaskMethod("worker-context-test")
    public String obiWan(WorkerContext context) {

        return ("hello there from wfRun "
                + context.getTaskRunId().getTaskGuid()
                + " on nodeRun "
                + context.getNodeRunId().getPosition());
    }
}
