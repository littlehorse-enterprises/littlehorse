import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.TaskStatus;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.sdk.worker.WorkerContext;
import io.littlehorse.test.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@LHTest
public class SequentialWorkflowTest {

    private WorkflowVerifier workflowVerifier;

    @LHWorkflow("simple-sequential-wf")
    private Workflow workflow;

    private WorkerContext context;

    @Test
    public void simpleSequentialWorkflowExecution() {
        workflowVerifier
                .prepare(workflow)
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
