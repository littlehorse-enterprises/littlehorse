import io.littlehorse.sdk.common.proto.CompleteUserTaskRunPb;
import io.littlehorse.sdk.common.proto.LHStatusPb;
import io.littlehorse.sdk.common.proto.PutExternalEventPb;
import io.littlehorse.sdk.common.proto.VarNameAndValPb;
import io.littlehorse.sdk.common.proto.VariableMutationTypePb;
import io.littlehorse.sdk.common.proto.VariableTypePb;
import io.littlehorse.sdk.usertask.annotations.UserTaskField;
import io.littlehorse.sdk.wfsdk.UserTaskOutput;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.sdk.worker.WorkerContext;
import io.littlehorse.test.*;
import java.util.function.Predicate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@LHTest
public class TestWf {

    private WorkflowExecutor workflowExecutor;

    @LHWorkflow("user-task-test-1")
    private Workflow workflow;

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
            "example-basic",
            thread -> {
                WfRunVariable theName = thread.addVariable(
                    "input-name",
                    VariableTypePb.STR
                );
                thread.execute("greet", theName);
            }
        );
    }

    public class AZUserTaskForm {

        @UserTaskField(
            displayName = "Str display name",
            description = "some discription"
        )
        public String myStr;

        @UserTaskField(
            displayName = "Int display name",
            description = "another discription"
        )
        public int myInt;
    }

    @LHTaskMethod("greet")
    public String greeting(String name) {
        System.out.println("executing");
        return "hello there, " + name;
    }

    @LHTaskMethod("az-describe-car")
    public String obiwan(AZUserTaskForm formData) {
        return "String was " + formData.myStr + " and int was " + formData.myInt;
    }

    @LHTaskMethod("az-reminder")
    public String reminder(WorkerContext workerContext) {
        Predicate<VarNameAndValPb> isUserGroupVariable = candidateVariable -> {
            return candidateVariable.getVarName().equals("userGroup");
        };
        String userGroupId = workerContext.getUserGroup().getId();
        return String.format("Hey there %s execute your task!", userGroupId);
    }
}
