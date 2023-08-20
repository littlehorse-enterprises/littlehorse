import io.littlehorse.sdk.common.proto.CompleteUserTaskRunRequest;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.PutExternalEventRequest;
import io.littlehorse.sdk.common.proto.VarNameAndVal;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.usertask.annotations.UserTaskField;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.sdk.worker.WorkerContext;
import io.littlehorse.test.*;
import java.util.function.Predicate;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@LHTest
@Disabled("WIP!")
public class TestWf {

    private WorkflowExecutor workflowExecutor;

    @LHWorkflow("user-task-test-1")
    private Workflow workflow;

    @Test
    public void test1() {
        CompleteUserTaskRunRequest completeUserTaskRun = null;
        PutExternalEventRequest externalEvent = null;
        WfRunVerifier verify = workflowExecutor
            .prepare(workflow)
            .waitForStatus(LHStatus.RUNNING)
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
                    VariableType.STR
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
        Predicate<VarNameAndVal> isUserGroupVariable = candidateVariable -> {
            return candidateVariable.getVarName().equals("userGroup");
        };
        String userGroupId = workerContext.getUserGroup().getId();
        return String.format("Hey there %s execute your task!", userGroupId);
    }
}
