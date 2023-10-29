package e2e;

import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.TaskStatus;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.sdk.worker.WorkerContext;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.LHWorkflow;
import io.littlehorse.test.WorkflowVerifier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@LHTest
public class SequentialWorkflowTest {

    private WorkflowVerifier workflowVerifier;

    @LHWorkflow("simple-sequential-wf")
    private Workflow workflow;

    @Test
    public void simpleSequentialWorkflowExecution() {
        workflowVerifier
                .prepareRun(workflow)
                .waitForStatus(LHStatus.COMPLETED)
                .waitForTaskStatus(0, 1, TaskStatus.TASK_SUCCESS)
                .waitForTaskStatus(0, 2, TaskStatus.TASK_SUCCESS)
                .thenVerifyTaskRunResult(
                        0,
                        1,
                        variableValue ->
                                Assertions.assertTrue(variableValue.getStr().contains("hello there from wfRun")))
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

    @LHWorkflow("simple-sequential-wf")
    public Workflow buildWorkflow() {
        return new WorkflowImpl("simple-sequential-wf", thread -> {
            thread.execute("aa-simple");
            thread.execute("aa-simple");
        });
    }

    @LHTaskMethod("aa-simple")
    public String obiWan(WorkerContext context) {

        return ("hello there from wfRun "
                + context.getTaskRunId().getTaskGuid()
                + " on nodeRun "
                + context.getNodeRunId().getPosition());
    }
}
