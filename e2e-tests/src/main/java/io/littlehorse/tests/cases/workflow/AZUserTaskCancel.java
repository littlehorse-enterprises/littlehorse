package io.littlehorse.tests.cases.workflow;

import io.littlehorse.sdk.client.LHClient;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.config.LHWorkerConfig;
import io.littlehorse.sdk.common.exception.LHApiError;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.AssignUserTaskRunPb;
import io.littlehorse.sdk.common.proto.AssignUserTaskRunReplyPb;
import io.littlehorse.sdk.common.proto.CancelUserTaskRunPb;
import io.littlehorse.sdk.common.proto.CancelUserTaskRunReplyPb;
import io.littlehorse.sdk.common.proto.CompleteUserTaskRunPb;
import io.littlehorse.sdk.common.proto.LHResponseCodePb;
import io.littlehorse.sdk.common.proto.LHStatusPb;
import io.littlehorse.sdk.common.proto.NodeRunPb;
import io.littlehorse.sdk.common.proto.SearchUserTaskRunPb;
import io.littlehorse.sdk.common.proto.SearchUserTaskRunReplyPb;
import io.littlehorse.sdk.common.proto.TaskRunIdPb;
import io.littlehorse.sdk.common.proto.TaskRunPb;
import io.littlehorse.sdk.common.proto.TaskStatusPb;
import io.littlehorse.sdk.common.proto.UserGroupPb;
import io.littlehorse.sdk.common.proto.UserPb;
import io.littlehorse.sdk.common.proto.UserTaskEventPb;
import io.littlehorse.sdk.common.proto.UserTaskEventPb.EventCase;
import io.littlehorse.sdk.common.proto.UserTaskFieldResultPb;
import io.littlehorse.sdk.common.proto.UserTaskResultPb;
import io.littlehorse.sdk.common.proto.UserTaskRunIdPb;
import io.littlehorse.sdk.common.proto.UserTaskRunPb;
import io.littlehorse.sdk.common.proto.UserTaskRunStatusPb;
import io.littlehorse.sdk.common.proto.VariableMutationTypePb;
import io.littlehorse.sdk.common.proto.VariableTypePb;
import io.littlehorse.sdk.wfsdk.UserTaskOutput;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.tests.TestFailure;
import io.littlehorse.tests.UserTaskWorkflowTest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class AZUserTaskCancel extends UserTaskWorkflowTest {

    private static final String USER_TASK_DEF_NAME = "some-usertask";

    public AZUserTaskCancel(LHClient client, LHWorkerConfig workerConfig) {
        super(client, workerConfig);
    }

    public String getDescription() {
        return (
            "Test for basic User Task assigned to specific user id, and tests " +
            " that we can schedule 'reminder tasks' which run X seconds after " +
            " the UserTaskRun is scheduled. "
        );
    }

    @Override
    public Map<String, Object> getRequiredUserTaskForms() {
        return Map.of(USER_TASK_DEF_NAME, new AZUserTaskForm());
    }

    @Override
    public Workflow getWorkflowImpl() {
        return new WorkflowImpl(
            getWorkflowName(),
            thread -> {
                WfRunVariable formVar = thread.addVariable(
                    "form",
                    VariableTypePb.JSON_OBJ
                );

                thread.assignUserTaskToUser(USER_TASK_DEF_NAME, "test-user");

                thread.execute("az-unreachable-task", formVar);
            }
        );
    }

    @Override
    public List<Object> getTaskWorkerObjects() {
        return Arrays.asList(new AZCancelTask());
    }

    public List<String> launchAndCheckWorkflows(LHClient client)
        throws TestFailure, InterruptedException, LHApiError {
        List<String> out = new ArrayList<>();

        String wfRunId = runWf(client);
        Thread.sleep(8 * 1000); // Wait for reminder task to execute

        // Get the UserTaskRun, ensure that there is an event with a taskRunId
        NodeRunPb firstUserTask = getNodeRun(client, wfRunId, 0, 1);
        UserTaskRunPb utr = getUserTaskRun(
            client,
            firstUserTask.getUserTask().getUserTaskRunId()
        );
        CancelUserTaskRunPb cancelUserTaskRunPb = CancelUserTaskRunPb
            .newBuilder()
            .setUserTaskRunId(utr.getId())
            .build();
        CancelUserTaskRunReplyPb cancelUserTaskRunReplyPb = client
            .getGrpcClient()
            .cancelUserTaskRun(cancelUserTaskRunPb);
        assertThat(
            cancelUserTaskRunReplyPb.getCode().equals(LHResponseCodePb.OK),
            "Error processing cancel UserTaskRun request"
        );
        assertStatus(client, wfRunId, LHStatusPb.ERROR);
        return out;
    }
}

class AZCancelTask {

    @LHTaskMethod("az-task-cancelled")
    public String obiwan(AZUserTaskForm formData) {
        return "String was " + formData.myStr + " and int was " + formData.myInt;
    }

    @LHTaskMethod("az-unreachable-task")
    public String unreachable(AZUserTaskForm formData) {
        return "Nothing to do...";
    }
}
