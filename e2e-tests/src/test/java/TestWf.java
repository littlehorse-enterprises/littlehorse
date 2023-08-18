import io.littlehorse.sdk.common.proto.CompleteUserTaskRunPb;
import io.littlehorse.sdk.common.proto.LHStatusPb;
import io.littlehorse.sdk.common.proto.PutExternalEventPb;
import io.littlehorse.sdk.common.proto.VariableMutationTypePb;
import io.littlehorse.sdk.common.proto.VariableTypePb;
import io.littlehorse.sdk.wfsdk.UserTaskOutput;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.test.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@LHTest
public class TestWf {

    private WorkflowExecutor workflowExecutor;

    @LHWorkflow("user-task-test-1")
    private Workflow workflow;

    @BeforeEach
    public void setup() {
        System.out.println("setup");
    }

    @Test
    public void test1() {
        CompleteUserTaskRunPb completeUserTaskRun = null;
        PutExternalEventPb externalEvent = null;
        WfRunVerifier verify = workflowExecutor
            .prepare(workflow)
            .waitForStatus(LHStatusPb.RUNNING)
            .andThenExecute(completeUserTaskRun)
            .andThenSend(externalEvent)
            .verify();
    }

    @LHWorkflow("user-task-test-1")
    public Workflow buildWorkflow() {
        return new WorkflowImpl(
            "user-task-test-1",
            thread -> {
                WfRunVariable formVar = thread.addVariable(
                    "form",
                    VariableTypePb.JSON_OBJ
                );

                UserTaskOutput formOutput = thread.assignUserTaskToUserGroup(
                    "it-request",
                    "test-group"
                );

                thread.scheduleReassignmentToUserOnDeadline(
                    formOutput,
                    "available-user",
                    5
                );

                thread.scheduleTaskAfter(formOutput, 2, "az-reminder");
                thread.mutate(formVar, VariableMutationTypePb.ASSIGN, formOutput);

                thread.execute("az-describe-car", formVar);
            }
        );
    }
}
