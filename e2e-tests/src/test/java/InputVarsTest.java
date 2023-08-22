import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.TaskStatus;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.util.Arg;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.sdk.worker.WorkerContext;
import io.littlehorse.test.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@LHTest
public class InputVarsTest {

    private WorkflowVerifier workflowVerifier;

    @LHWorkflow("input-vars-wf")
    private Workflow workflow;
    private WorkerContext context;

    @Test
    public void simpleSequentialWorkflowExecution() {
        workflowVerifier
            .prepare(workflow, Arg.of("my-var", 10))
            .waitForStatus(LHStatus.COMPLETED)
            .waitForTaskStatus(0, 1, TaskStatus.TASK_SUCCESS)
            .waitForTaskStatus(0, 2, TaskStatus.TASK_SUCCESS)
            .thenVerifyTaskRunResult(0, 1, variableValue -> Assertions.assertEquals(20, variableValue.getInt()))
            .thenVerifyTaskRunResult(0, 2, variableValue -> Assertions.assertEquals(2, variableValue.getInt()))
            .start();
    }

    @LHWorkflow("input-vars-wf")
    public Workflow buildWorkflow() {
        return new WorkflowImpl("input-vars-wf", thread -> {
            WfRunVariable myVar = thread.addVariable("my-var", VariableType.INT);
            thread.execute("ab-double-it", myVar);
            thread.execute("ab-subtract", 10, 8);
        });
    }
    @LHTaskMethod("ab-double-it")
    public int doubleIt(int toDouble) {
        return toDouble * 2;
    }

    @LHTaskMethod("ab-subtract")
    public Long subtract(long first, Integer second) {
        return first - second;
    }
}
