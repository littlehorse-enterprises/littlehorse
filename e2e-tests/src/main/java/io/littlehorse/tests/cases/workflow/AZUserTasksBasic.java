package io.littlehorse.tests.cases.workflow;

import io.littlehorse.sdk.client.LHClient;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.config.LHWorkerConfig;
import io.littlehorse.sdk.common.exception.LHApiError;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.AssignUserTaskRunRequest;
import io.littlehorse.sdk.common.proto.AssignUserTaskRunResponse;
import io.littlehorse.sdk.common.proto.CompleteUserTaskRunRequest;
import io.littlehorse.sdk.common.proto.CompleteUserTaskRunResponse;
import io.littlehorse.sdk.common.proto.LHResponseCode;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.NodeRun;
import io.littlehorse.sdk.common.proto.SearchUserTaskRunRequest;
import io.littlehorse.sdk.common.proto.SearchUserTaskRunResponse;
import io.littlehorse.sdk.common.proto.TaskRun;
import io.littlehorse.sdk.common.proto.TaskRunId;
import io.littlehorse.sdk.common.proto.TaskStatus;
import io.littlehorse.sdk.common.proto.User;
import io.littlehorse.sdk.common.proto.UserGroup;
import io.littlehorse.sdk.common.proto.UserTaskEvent;
import io.littlehorse.sdk.common.proto.UserTaskEvent.EventCase;
import io.littlehorse.sdk.common.proto.UserTaskFieldResult;
import io.littlehorse.sdk.common.proto.UserTaskResult;
import io.littlehorse.sdk.common.proto.UserTaskRun;
import io.littlehorse.sdk.common.proto.UserTaskRunId;
import io.littlehorse.sdk.common.proto.UserTaskRunStatus;
import io.littlehorse.sdk.common.proto.VarNameAndVal;
import io.littlehorse.sdk.common.proto.VariableMutationType;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.usertask.annotations.UserTaskField;
import io.littlehorse.sdk.wfsdk.UserTaskOutput;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.sdk.worker.WorkerContext;
import io.littlehorse.tests.TestFailure;
import io.littlehorse.tests.UserTaskWorkflowTest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class AZUserTasksBasic extends UserTaskWorkflowTest {

    private static final String USER_TASK_DEF_NAME = "some-usertask";

    public AZUserTasksBasic(LHClient client, LHWorkerConfig workerConfig) {
        super(client, workerConfig);
    }

    public String getDescription() {
        return ("Test for basic User Task assigned to specific user id, and tests "
                + " that we can schedule 'reminder tasks' which run X seconds after "
                + " the UserTaskRun is scheduled. ");
    }

    @Override
    public Map<String, Object> getRequiredUserTaskForms() {
        return Map.of(USER_TASK_DEF_NAME, new AZUserTaskForm());
    }

    @Override
    public Workflow getWorkflowImpl() {
        return new WorkflowImpl(getWorkflowName(), thread -> {
            WfRunVariable formVar = thread.addVariable("form", VariableType.JSON_OBJ);

            UserTaskOutput formOutput = thread.assignUserTaskToUserGroup(USER_TASK_DEF_NAME, "test-group");

            thread.scheduleReassignmentToUserOnDeadline(formOutput, "available-user", 5);

            thread.scheduleTaskAfter(formOutput, 2, "az-reminder");
            thread.mutate(formVar, VariableMutationType.ASSIGN, formOutput);

            thread.execute("az-describe-car", formVar);
        });
    }

    @Override
    public List<Object> getTaskWorkerObjects() {
        return Arrays.asList(new AZSimpleTask());
    }

    public List<String> launchAndCheckWorkflows(LHClient client) throws TestFailure, InterruptedException, LHApiError {
        List<String> out = new ArrayList<>();

        String wfRunId = runWf(client);
        Thread.sleep(8 * 1000); // Wait for reminder task to execute

        // Get the UserTaskRun, ensure that there is an event with a taskRunId
        NodeRun firstUserTask = getNodeRun(client, wfRunId, 0, 1);
        UserTaskRun utr = getUserTaskRun(client, firstUserTask.getUserTask().getUserTaskRunId());

        if (utr.getEventsCount() < 2) {
            throw new TestFailure(
                    this, "Workflow " + wfRunId + " should have a triggered task run event on the first noderun!");
        }
        UserTaskEvent ute = utr.getEvents(1);
        if (!ute.getEventCase().equals(EventCase.TASK_EXECUTED)) {
            throw new TestFailure(this, "Workflow " + wfRunId + " usertask run doesn't have proper event set!");
        }

        // Now we get the TaskRun for that event.
        TaskRunId executedTaskRunId = ute.getTaskExecuted().getTaskRun();
        TaskRun taskRun = getTaskRun(client, executedTaskRunId);
        if (taskRun.getAttempts(0).getStatus() != TaskStatus.TASK_SUCCESS) {
            throw new TestFailure(this, "Workflow " + wfRunId + " usertask run reminder didn't complete!");
        }

        SearchUserTaskRunResponse userGroupResult = client.getGrpcClient()
                .searchUserTaskRun(SearchUserTaskRunRequest.newBuilder()
                        .setUserGroup(UserGroup.newBuilder().setId("test-group").build())
                        .setUserTaskDefName(USER_TASK_DEF_NAME)
                        .setStatus(UserTaskRunStatus.UNASSIGNED)
                        .build());
        UserTaskRunId userTaskRunId = null;
        for (UserTaskRunId userTaskRunIdResult : userGroupResult.getResultsList()) {
            if (userTaskRunIdResult.getWfRunId().equals(wfRunId)) {
                userTaskRunId = userTaskRunIdResult;
                break;
            }
        }
        AssignUserTaskRunResponse assignUserTaskRunResponse = client.getGrpcClient()
                .assignUserTaskRun(AssignUserTaskRunRequest.newBuilder()
                        .setUser(User.newBuilder().setId("unavailable-user").build())
                        .setUserTaskRunId(userTaskRunId)
                        .build());
        assertThat(
                assignUserTaskRunResponse.getCode() == LHResponseCode.OK,
                "Unexpected response from user assignment request");
        Thread.sleep(1000 * 10);

        // Look for UserTaskRun's with `test-user` as the user
        SearchUserTaskRunResponse results = client.getGrpcClient()
                .searchUserTaskRun(SearchUserTaskRunRequest.newBuilder()
                        .setUser(User.newBuilder()
                                .setId("available-user")
                                .setUserGroup(UserGroup.newBuilder()
                                        .setId("test-group")
                                        .build()))
                        .build());
        assertThat(results.getCode() == LHResponseCode.OK, "Unexpected response from search request");
        UserTaskRunId found = null;

        for (UserTaskRunId candidate : results.getResultsList()) {
            if (candidate.getWfRunId().equals(wfRunId)) {
                found = candidate;
                break;
            }
        }

        if (found == null) {
            throw new TestFailure(this, "Couldn't find available-user's task!");
        }

        // Now we execute the task
        try {
            CompleteUserTaskRunResponse completeUserTaskRunResponse =
                    client.getGrpcClient().completeUserTaskRun(buildInvalidCompleteUserTaskRequest(found));
            assertThat(
                    completeUserTaskRunResponse.getCode().equals(LHResponseCode.BAD_REQUEST_ERROR),
                    "UserTaskRun Fields validation not working as expected");
            assertThat(
                    completeUserTaskRunResponse
                            .getMessage()
                            .equals("Field [name = nonExistingStringField, type = STR] is not"
                                    + " defined in UserTask schema"),
                    "Actual output message: " + completeUserTaskRunResponse.getMessage());
            completeUserTaskRunResponse =
                    client.getGrpcClient().completeUserTaskRun(buildCompleteUserTaskRequestWithMissingField(found));
            assertThat(
                    completeUserTaskRunResponse.getCode().equals(LHResponseCode.BAD_REQUEST_ERROR),
                    "UserTaskRun mandatory fields validation is not working as expected");
            assertThat(
                    completeUserTaskRunResponse.getMessage().equals("[myStr] are mandatory fields"),
                    "Actual output message: " + completeUserTaskRunResponse.getMessage());
            completeUserTaskRunResponse =
                    client.getGrpcClient().completeUserTaskRun(buildValidCompleteUserTaskRequest(found));
            assertThat(
                    completeUserTaskRunResponse.getCode().equals(LHResponseCode.OK),
                    "Failed processing complete user task request");
        } catch (LHSerdeError exn) {
            throw new RuntimeException(exn);
        }

        // Wait for the last task to complete
        Thread.sleep(200);

        assertStatus(client, wfRunId, LHStatus.COMPLETED);

        assertTaskOutputsMatch(client, wfRunId, 0, "String was asdf and int was 123");

        out.add(wfRunId);
        return out;
    }

    private CompleteUserTaskRunRequest buildValidCompleteUserTaskRequest(UserTaskRunId userTaskRUnIdToComplete)
            throws LHSerdeError {
        return CompleteUserTaskRunRequest.newBuilder()
                .setUserTaskRunId(userTaskRUnIdToComplete)
                .setResult(UserTaskResult.newBuilder()
                        .addFields(UserTaskFieldResult.newBuilder()
                                .setName("myStr")
                                .setValue(LHLibUtil.objToVarVal("asdf")))
                        .addFields(UserTaskFieldResult.newBuilder()
                                .setName("myInt")
                                .setValue(LHLibUtil.objToVarVal(123))))
                .build();
    }

    private CompleteUserTaskRunRequest buildInvalidCompleteUserTaskRequest(UserTaskRunId userTaskRUnIdToComplete)
            throws LHSerdeError {
        return CompleteUserTaskRunRequest.newBuilder()
                .setUserTaskRunId(userTaskRUnIdToComplete)
                .setResult(UserTaskResult.newBuilder()
                        .addFields(UserTaskFieldResult.newBuilder()
                                .setName("nonExistingStringField")
                                .setValue(LHLibUtil.objToVarVal("asdf")))
                        .addFields(UserTaskFieldResult.newBuilder()
                                .setName("myInt")
                                .setValue(LHLibUtil.objToVarVal(123))))
                .build();
    }

    private CompleteUserTaskRunRequest buildCompleteUserTaskRequestWithMissingField(
            UserTaskRunId userTaskRUnIdToComplete) throws LHSerdeError {
        return CompleteUserTaskRunRequest.newBuilder()
                .setUserTaskRunId(userTaskRUnIdToComplete)
                .setResult(UserTaskResult.newBuilder()
                        .addFields(UserTaskFieldResult.newBuilder()
                                .setName("myInt")
                                .setValue(LHLibUtil.objToVarVal(123))))
                .build();
    }
}

class AZUserTaskForm {

    @UserTaskField(displayName = "Str display name", description = "some discription")
    public String myStr;

    @UserTaskField(displayName = "Int display name", description = "another discription")
    public int myInt;
}

class AZSimpleTask {

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
