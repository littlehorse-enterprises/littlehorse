package e2e;

import static io.littlehorse.sdk.common.proto.LHStatus.*;

import io.littlehorse.sdk.common.proto.UserTaskRunStatus;
import io.littlehorse.sdk.common.proto.VariableMutationType;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.usertask.annotations.UserTaskField;
import io.littlehorse.sdk.wfsdk.UserTaskOutput;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.LHUserTaskForm;
import io.littlehorse.test.LHWorkflow;
import io.littlehorse.test.WorkflowVerifier;
import java.time.Duration;
import org.junit.jupiter.api.Test;

@LHTest
public class UserTaskTest {

    public static final String USER_TASK_DEF_NAME = "my-usertask";

    @LHWorkflow("deadline-reassignment-workflow")
    private Workflow workflow;

    @LHUserTaskForm(USER_TASK_DEF_NAME)
    private MyForm myForm = new MyForm();

    private WorkflowVerifier workflowVerifier;

    @Test
    void shouldTransferOwnershipFromUserToGroupOnDeadline() {
        workflowVerifier
                .prepareRun(workflow)
                .waitForStatus(RUNNING)
                .waitForUserTaskRunStatus(0, 1, UserTaskRunStatus.ASSIGNED)
                .waitForUserTaskRunStatus(0, 1, UserTaskRunStatus.UNASSIGNED, Duration.ofSeconds(6))
                .start();
    }

    @LHWorkflow("deadline-reassignment-workflow")
    public Workflow buildDeadlineReassignmentWorkflow() {
        return new WorkflowImpl("deadline-reassignment-workflow", entrypointThread -> {
            WfRunVariable formVar = entrypointThread.addVariable("form", VariableType.JSON_OBJ);

            UserTaskOutput formOutput =
                    entrypointThread.assignTaskToUser(USER_TASK_DEF_NAME, "test-group", "test-department");

            entrypointThread.reassignToGroupOnDeadline(formOutput, 4);

            entrypointThread.mutate(formVar, VariableMutationType.ASSIGN, formOutput);

            entrypointThread.execute("my-task", formVar);
        });
    }

    @LHTaskMethod("my-task")
    public String obiwan(MyForm formData) {
        return "String was " + formData.myStr + " and int was " + formData.myInt;
    }

    public class MyForm {

        @UserTaskField(displayName = "Str display name", description = "some discription")
        public String myStr;

        @UserTaskField(displayName = "Int display name", description = "another discription")
        public int myInt;

        public MyForm() {}
    }
}
