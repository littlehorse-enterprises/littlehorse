package e2e;

import static io.littlehorse.sdk.common.proto.LHStatus.*;

import io.littlehorse.sdk.common.proto.SearchWfRunRequest;
import io.littlehorse.sdk.common.proto.UserTaskRunStatus;
import io.littlehorse.sdk.common.proto.VariableMutationType;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.WfRunIdList;
import io.littlehorse.sdk.usertask.annotations.UserTaskField;
import io.littlehorse.sdk.wfsdk.UserTaskOutput;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.test.CapturedResult;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.LHUserTaskForm;
import io.littlehorse.test.LHWorkflow;
import io.littlehorse.test.SearchResultCaptor;
import io.littlehorse.test.WorkflowVerifier;
import io.littlehorse.test.internal.TestExecutionContext;
import java.time.Duration;
import java.util.function.Function;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

@LHTest
public class UserTaskTest {

    public static final String USER_TASK_DEF_NAME = "my-usertask";

    @LHWorkflow("deadline-reassignment-workflow")
    private Workflow deadlineReassignmentWorkflow;

    @LHWorkflow("deadline-reassignment-workflow-user-without-group")
    private Workflow deadlineReassignmentUserWithoutGroupWorkflow;

    @LHUserTaskForm(USER_TASK_DEF_NAME)
    private MyForm myForm = new MyForm();

    private WorkflowVerifier workflowVerifier;

    @Test
    void shouldTransferOwnershipFromUserToGroupOnDeadline() {
        workflowVerifier
                .prepareRun(deadlineReassignmentWorkflow)
                .waitForStatus(RUNNING)
                .waitForUserTaskRunStatus(0, 1, UserTaskRunStatus.ASSIGNED)
                .waitForUserTaskRunStatus(0, 1, UserTaskRunStatus.UNASSIGNED, Duration.ofSeconds(6))
                .start();
    }

    @Test
    void shouldTransferOwnershipFromUserToSpecificGroupOnDeadline() {
        SearchResultCaptor<WfRunIdList> instanceCaptor = SearchResultCaptor.of(WfRunIdList.class);
        Function<TestExecutionContext, SearchWfRunRequest> buildId = context -> SearchWfRunRequest.newBuilder()
                .setWfSpecName("deadline-reassignment-workflow-user-without-group")
                .setStatus(RUNNING)
                .build();
        workflowVerifier
                .prepareRun(deadlineReassignmentUserWithoutGroupWorkflow)
                .waitForStatus(RUNNING)
                .doSearch(SearchWfRunRequest.class, instanceCaptor.capture(), buildId)
                .waitForUserTaskRunStatus(0, 1, UserTaskRunStatus.ASSIGNED)
                .waitForUserTaskRunStatus(0, 1, UserTaskRunStatus.UNASSIGNED, Duration.ofSeconds(6))
                .start();
        CapturedResult<WfRunIdList> capturedResult = instanceCaptor.getValue();
        WfRunIdList wfRunIdList = capturedResult.get();
        Assertions.assertThat(wfRunIdList).isNotNull();
        Assertions.assertThat(wfRunIdList.getResultsList()).isNotEmpty();
    }

    @LHWorkflow("deadline-reassignment-workflow")
    public Workflow buildDeadlineReassignmentWorkflow() {
        return new WorkflowImpl("deadline-reassignment-workflow", entrypointThread -> {
            WfRunVariable formVar = entrypointThread.addVariable("form", VariableType.JSON_OBJ);

            UserTaskOutput formOutput =
                    entrypointThread.assignUserTask(USER_TASK_DEF_NAME, "test-group", "test-department");

            entrypointThread.releaseToGroupOnDeadline(formOutput, 1);

            entrypointThread.mutate(formVar, VariableMutationType.ASSIGN, formOutput);

            entrypointThread.execute("my-custom-task", formVar);
        });
    }

    @LHWorkflow("deadline-reassignment-workflow-user-without-group")
    public Workflow buildDeadlineReassignmentWorkflowUserWithoutGroup() {
        return new WorkflowImpl("deadline-reassignment-workflow-user-without-group", entrypointThread -> {
            WfRunVariable formVar = entrypointThread.addVariable("form", VariableType.JSON_OBJ);

            UserTaskOutput formOutput = entrypointThread.assignUserTask(USER_TASK_DEF_NAME, "test-user-id", null);

            entrypointThread.reassignUserTask(formOutput, null, "test-it-department", 1);

            entrypointThread.mutate(formVar, VariableMutationType.ASSIGN, formOutput);

            entrypointThread.execute("my-custom-task", formVar);
        });
    }

    @LHTaskMethod("my-custom-task")
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
