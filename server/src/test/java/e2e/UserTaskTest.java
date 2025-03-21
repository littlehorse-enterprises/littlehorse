package e2e;

import static io.littlehorse.sdk.common.proto.LHStatus.*;

import io.grpc.Status.Code;
import io.grpc.StatusRuntimeException;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.proto.CompleteUserTaskRunRequest;
import io.littlehorse.sdk.common.proto.Failure;
import io.littlehorse.sdk.common.proto.ListUserTaskRunRequest;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.NodeRun.NodeTypeCase;
import io.littlehorse.sdk.common.proto.SaveUserTaskRunProgressRequest;
import io.littlehorse.sdk.common.proto.SaveUserTaskRunProgressRequest.SaveUserTaskRunAssignmentPolicy;
import io.littlehorse.sdk.common.proto.SearchWfRunRequest;
import io.littlehorse.sdk.common.proto.TaskRun;
import io.littlehorse.sdk.common.proto.TaskRunId;
import io.littlehorse.sdk.common.proto.TaskStatus;
import io.littlehorse.sdk.common.proto.UserTaskEvent;
import io.littlehorse.sdk.common.proto.UserTaskEvent.EventCase;
import io.littlehorse.sdk.common.proto.UserTaskRun;
import io.littlehorse.sdk.common.proto.UserTaskRunId;
import io.littlehorse.sdk.common.proto.UserTaskRunStatus;
import io.littlehorse.sdk.common.proto.VariableMutationType;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.WfRunId;
import io.littlehorse.sdk.common.proto.WfRunIdList;
import io.littlehorse.sdk.usertask.annotations.UserTaskField;
import io.littlehorse.sdk.wfsdk.UserTaskOutput;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.sdk.worker.WorkerContext;
import io.littlehorse.test.CapturedResult;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.LHUserTaskForm;
import io.littlehorse.test.LHWorkflow;
import io.littlehorse.test.WorkflowVerifier;
import io.littlehorse.test.internal.TestExecutionContext;
import io.littlehorse.test.internal.step.SearchResultCaptor;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

@LHTest
public class UserTaskTest {

    public static final String USER_TASK_DEF_NAME = "my-usertask";
    private static final String TEST_USER_ID = "test-user-id";

    private static final ConcurrentHashMap<String, String> cache = new ConcurrentHashMap<>();

    @LHWorkflow("deadline-reassignment-workflow")
    private Workflow deadlineReassignmentWorkflow;

    @LHWorkflow("deadline-reassignment-workflow-user-without-group")
    private Workflow deadlineReassignmentUserWithoutGroupWorkflow;

    @LHWorkflow("cancel-user-task")
    private Workflow userTaskCancel;

    @LHWorkflow("cancel-user-task-on-deadline")
    private Workflow userTaskCancelOnDeadline;

    @LHWorkflow("schedule-reminder-task-without-user-fields-workflow")
    private Workflow scheduleReminderTaskWithoutUserFields;

    @LHWorkflow("worker-context-receives-user-details")
    private Workflow workerContextReceivesUserDetails;

    @LHWorkflow("user-task-assignment")
    private Workflow userTaskAssignment;

    @LHUserTaskForm(USER_TASK_DEF_NAME)
    private MyForm myForm = new MyForm();

    private WorkflowVerifier workflowVerifier;
    private LittleHorseBlockingStub client;

    @Test
    void shouldCompleteUserTaskRunWithProperOutput() {
        workflowVerifier
                .prepareRun(deadlineReassignmentWorkflow)
                .waitForStatus(RUNNING)
                .thenVerifyWfRun(wfRun -> {
                    // Complete the UserTaskRun
                    UserTaskRunId userTaskRunId = client.listUserTaskRuns(ListUserTaskRunRequest.newBuilder()
                                    .setWfRunId(wfRun.getId())
                                    .build())
                            .getResultsList()
                            .get(0)
                            .getId();

                    client.completeUserTaskRun(CompleteUserTaskRunRequest.newBuilder()
                            .setUserTaskRunId(userTaskRunId)
                            .setUserId("obiwan")
                            .putResults("myStr", LHLibUtil.objToVarVal("kenobi"))
                            .putResults("myInt", LHLibUtil.objToVarVal(137))
                            .build());
                })
                .waitForStatus(COMPLETED)
                .thenVerifyTaskRun(0, 2, taskRun -> {
                    String taskResult = taskRun.getAttempts(0).getOutput().getStr();
                    Assertions.assertThat(taskResult).contains("kenobi");
                    Assertions.assertThat(taskResult).contains("137");
                })
                .start();
    }

    @Test
    void shouldSaveUserTaskRun() {
        workflowVerifier
                .prepareRun(userTaskCancel)
                .waitForStatus(RUNNING)
                .thenVerifyWfRun(wfRun -> {
                    // Complete the UserTaskRun
                    UserTaskRunId userTaskRunId = client.listUserTaskRuns(ListUserTaskRunRequest.newBuilder()
                                    .setWfRunId(wfRun.getId())
                                    .build())
                            .getResultsList()
                            .get(0)
                            .getId();

                    // TODO: make this a Step
                    client.saveUserTaskRunProgress(SaveUserTaskRunProgressRequest.newBuilder()
                            .setUserTaskRunId(userTaskRunId)
                            .setUserId(TEST_USER_ID)
                            .putResults("myStr", LHLibUtil.objToVarVal("hello there"))
                            .setPolicy(SaveUserTaskRunAssignmentPolicy.FAIL_IF_CLAIMED_BY_OTHER)
                            .build());
                })
                .thenVerifyNodeRun(0, 1, nodeRun -> {
                    Assertions.assertThat(nodeRun.getNodeTypeCase()).isEqualTo(NodeTypeCase.USER_TASK);
                    UserTaskRun userTask =
                            client.getUserTaskRun(nodeRun.getUserTask().getUserTaskRunId());
                    Assertions.assertThat(userTask.getResultsMap().get("myStr").getStr())
                            .isEqualTo("hello there");
                    Assertions.assertThat(userTask.getEventsCount()).isEqualTo(2);

                    UserTaskEvent savedEvent = userTask.getEvents(1);
                    Assertions.assertThat(savedEvent.getEventCase()).isEqualTo(EventCase.SAVED);
                    Assertions.assertThat(savedEvent.getSaved().getUserId()).isEqualTo(TEST_USER_ID);
                    Assertions.assertThat(savedEvent
                                    .getSaved()
                                    .getResultsMap()
                                    .get("myStr")
                                    .getStr())
                            .isEqualTo("hello there");
                })
                // No need to complete the user task run for this test.
                .start();
    }

    @Test
    void shouldNotSaveUserTaskRunIfUserIdNotMatchAndFailIfClaimedByOther() {
        workflowVerifier
                .prepareRun(userTaskCancel)
                .waitForStatus(RUNNING)
                .thenVerifyWfRun(wfRun -> {
                    // Complete the UserTaskRun
                    UserTaskRunId userTaskRunId = client.listUserTaskRuns(ListUserTaskRunRequest.newBuilder()
                                    .setWfRunId(wfRun.getId())
                                    .build())
                            .getResultsList()
                            .get(0)
                            .getId();

                    Assertions.assertThatThrownBy(
                                    () -> client.saveUserTaskRunProgress(SaveUserTaskRunProgressRequest.newBuilder()
                                            .setUserTaskRunId(userTaskRunId)
                                            .setUserId("not-the-same-user-id-who-owns-it")
                                            .putResults("myStr", LHLibUtil.objToVarVal("hello there"))
                                            .setPolicy(SaveUserTaskRunAssignmentPolicy.FAIL_IF_CLAIMED_BY_OTHER)
                                            .build()))
                            .matches(exn -> {
                                Assertions.assertThat(StatusRuntimeException.class)
                                        .isAssignableFrom(exn.getClass());

                                StatusRuntimeException sre = (StatusRuntimeException) exn;
                                Assertions.assertThat(sre.getStatus().getCode()).isEqualTo(Code.FAILED_PRECONDITION);
                                Assertions.assertThat(
                                                sre.getStatus().getDescription().toLowerCase())
                                        .contains("another user");
                                return true;
                            });
                })
                .thenVerifyNodeRun(0, 1, nodeRun -> {
                    // Verify that no event was saved
                    Assertions.assertThat(nodeRun.getNodeTypeCase()).isEqualTo(NodeTypeCase.USER_TASK);
                    UserTaskRun userTask =
                            client.getUserTaskRun(nodeRun.getUserTask().getUserTaskRunId());
                    Assertions.assertThat(userTask.getResultsMap().get("myStr")).isNull();
                    ;
                    Assertions.assertThat(userTask.getEventsCount()).isEqualTo(1);
                })
                // No need to complete the user task run for this test.
                .start();
    }

    @Test
    void shouldSaveButNotClaimUserTaskIfSetToIgnoreClaim() {
        String userId = "mace-windu";
        workflowVerifier
                .prepareRun(userTaskCancel)
                .waitForStatus(RUNNING)
                .thenVerifyWfRun(wfRun -> {
                    // Complete the UserTaskRun
                    UserTaskRunId userTaskRunId = client.listUserTaskRuns(ListUserTaskRunRequest.newBuilder()
                                    .setWfRunId(wfRun.getId())
                                    .build())
                            .getResultsList()
                            .get(0)
                            .getId();

                    // TODO: make this a Step
                    client.saveUserTaskRunProgress(SaveUserTaskRunProgressRequest.newBuilder()
                            .setUserTaskRunId(userTaskRunId)
                            .setUserId(userId)
                            .putResults("myStr", LHLibUtil.objToVarVal("hello there"))
                            .setPolicy(SaveUserTaskRunAssignmentPolicy.IGNORE_CLAIM)
                            .build());
                })
                .thenVerifyNodeRun(0, 1, nodeRun -> {
                    Assertions.assertThat(nodeRun.getNodeTypeCase()).isEqualTo(NodeTypeCase.USER_TASK);
                    UserTaskRun userTask =
                            client.getUserTaskRun(nodeRun.getUserTask().getUserTaskRunId());
                    Assertions.assertThat(userTask.getResultsMap().get("myStr").getStr())
                            .isEqualTo("hello there");
                    Assertions.assertThat(userTask.getEventsCount()).isEqualTo(2);

                    UserTaskEvent savedEvent = userTask.getEvents(1);
                    Assertions.assertThat(savedEvent.getEventCase()).isEqualTo(EventCase.SAVED);
                    Assertions.assertThat(savedEvent.getSaved().getUserId()).isEqualTo(userId);
                    Assertions.assertThat(savedEvent
                                    .getSaved()
                                    .getResultsMap()
                                    .get("myStr")
                                    .getStr())
                            .isEqualTo("hello there");

                    // Ensure that the UserTaskRun is NOT assigned to Mace Windu
                    Assertions.assertThat(userTask.getUserId()).isEqualTo(TEST_USER_ID);
                })
                // No need to complete the user task run for this test.
                .start();
    }

    @Test
    void shouldTransferOwnershipFromUserToGroupOnDeadline() {
        WfRunId id = WfRunId.newBuilder().setId(LHUtil.generateGuid()).build();

        workflowVerifier
                .prepareRun(deadlineReassignmentWorkflow)
                .waitForStatus(RUNNING)
                .waitForUserTaskRunStatus(0, 1, UserTaskRunStatus.ASSIGNED)
                .waitForUserTaskRunStatus(0, 1, UserTaskRunStatus.UNASSIGNED, Duration.ofSeconds(6))
                .thenVerifyNodeRun(0, 1, nodeRun -> {
                    UserTaskRun userTask =
                            client.getUserTaskRun(nodeRun.getUserTask().getUserTaskRunId());

                    // Ensure that the reminder task was executed
                    Assertions.assertThat(cache).containsKey(id.getId());
                    Assertions.assertThat(userTask.getEventsCount()).isEqualTo(3);
                    UserTaskEvent reminderTaskEvent = userTask.getEvents(1);
                    Assertions.assertThat(reminderTaskEvent.getEventCase()).isEqualTo(EventCase.TASK_EXECUTED);
                })
                .start(id);
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
                .thenCancelUserTaskRun(0, 1)
                .start();
        CapturedResult<WfRunIdList> capturedResult = instanceCaptor.getValue();
        WfRunIdList wfRunIdList = capturedResult.get();
        Assertions.assertThat(wfRunIdList).isNotNull();
        Assertions.assertThat(wfRunIdList.getResultsList()).isNotEmpty();
    }

    @Test
    void shouldExecuteBusinessExceptionHandlerWhenUserTaskGetsCancel() {
        workflowVerifier
                .prepareRun(userTaskCancel)
                .thenCancelUserTaskRun(0, 1)
                .waitForStatus(COMPLETED)
                .start();
    }

    @Test
    void shouldExecuteBusinessExceptionHandlerWhenUserTaskGetsCancelOnDeadline() {
        workflowVerifier
                .prepareRun(userTaskCancelOnDeadline)
                .waitForStatus(COMPLETED, Duration.ofSeconds(5))
                .start();
    }

    @Test
    void shouldScheduleAndExecuteReminderTask() {
        workflowVerifier
                .prepareRun(scheduleReminderTaskWithoutUserFields)
                .waitForStatus(ERROR, Duration.ofSeconds(6))
                .thenVerifyNodeRun(0, 1, nodeRun -> {
                    UserTaskRunId userTaskId = nodeRun.getUserTask().getUserTaskRunId();
                    UserTaskRun userTaskRun = client.getUserTaskRun(userTaskId);
                    UserTaskEvent userTaskEvent = userTaskRun.getEvents(1);
                    TaskRunId taskRunId = userTaskEvent.getTaskExecuted().getTaskRun();
                    TaskRun taskRun = client.getTaskRun(taskRunId);
                    TaskStatus taskRunStatus = taskRun.getStatus();

                    Assertions.assertThat(taskRunStatus).isEqualTo(TaskStatus.TASK_SUCCESS);
                })
                .start();
    }

    @Test
    void shouldValidateUserIdOrUserGroup() {
        workflowVerifier
                .prepareRun(userTaskAssignment)
                .waitForStatus(ERROR)
                .waitForNodeRunStatus(0, 1, ERROR)
                .thenVerifyNodeRun(0, 1, nodeRun -> {
                    List<Failure> failures = nodeRun.getFailuresList();
                    Assertions.assertThat(failures).hasSize(1);
                    Failure nodeFailure = failures.get(0);
                    Assertions.assertThat(nodeFailure.getFailureName()).isEqualTo("VAR_ERROR");
                    Assertions.assertThat(nodeFailure.getMessage()).isEqualTo("Invalid user task assignment");
                })
                .start();
    }

    @Test
    void verifyWorkerContextHasUserIdOrUserGroup() {
        workflowVerifier
                .prepareRun(workerContextReceivesUserDetails)
                .waitForStatus(ERROR, Duration.ofSeconds(6))
                .thenVerifyNodeRun(0, 1, nodeRun -> {
                    UserTaskRunId userTaskId = nodeRun.getUserTask().getUserTaskRunId();
                    UserTaskRun userTaskRun = client.getUserTaskRun(userTaskId);
                    UserTaskEvent userTaskEvent = userTaskRun.getEvents(1);
                    TaskRunId taskRunId = userTaskEvent.getTaskExecuted().getTaskRun();
                    TaskRun taskRun = client.getTaskRun(taskRunId);
                    TaskStatus taskRunStatus = taskRun.getStatus();

                    Assertions.assertThat(taskRunStatus).isEqualTo(TaskStatus.TASK_SUCCESS);
                })
                .start();
    }

    @LHWorkflow("deadline-reassignment-workflow")
    public Workflow buildDeadlineReassignmentWorkflow() {
        return new WorkflowImpl("deadline-reassignment-workflow", entrypointThread -> {
            WfRunVariable formVar = entrypointThread.addVariable("form", VariableType.JSON_OBJ);

            UserTaskOutput formOutput =
                    entrypointThread.assignUserTask(USER_TASK_DEF_NAME, "test-group", "test-department");

            // Schedule a reminder immediately
            entrypointThread.scheduleReminderTask(formOutput, 0, "reminder-task");

            entrypointThread.releaseToGroupOnDeadline(formOutput, 1);

            entrypointThread.mutate(formVar, VariableMutationType.ASSIGN, formOutput);

            entrypointThread.execute("my-custom-task", formVar);
        });
    }

    @LHWorkflow("schedule-reminder-task-without-user-fields-workflow")
    public Workflow buildReminderTaskWorkflowWithUserGroupField() {
        return new WorkflowImpl("reminder-task-without-user-fields-workflow", entrypointThread -> {
            UserTaskOutput formOutput = entrypointThread.assignUserTask(USER_TASK_DEF_NAME, "jacob", null);

            // Schedule a reminder immediately
            entrypointThread.scheduleReminderTask(formOutput, 0, "reminder-task");

            entrypointThread.cancelUserTaskRunAfter(formOutput, 5);
        });
    }

    @LHWorkflow("worker-context-receives-user-details")
    public Workflow workerContextReceivesUserDetails() {
        return new WorkflowImpl("worker-context-receives-user-details", entrypointThread -> {
            UserTaskOutput formOutput = entrypointThread.assignUserTask(USER_TASK_DEF_NAME, "jacob", null);

            // Schedule a reminder immediately
            entrypointThread.scheduleReminderTask(formOutput, 0, "verify-worker-context", "jacob", null);

            entrypointThread.cancelUserTaskRunAfter(formOutput, 5);
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

    @LHWorkflow("cancel-user-task")
    public Workflow buildCancelUserTaskWorkflow() {
        return new WorkflowImpl("cancel-user-task", entrypointThread -> {
            UserTaskOutput formOutput = entrypointThread
                    .assignUserTask(USER_TASK_DEF_NAME, "test-user-id", null)
                    .withOnCancellationException("no-response");
            entrypointThread.handleException(formOutput, "no-response", userTaskCanceledHandler -> {
                userTaskCanceledHandler.execute("user-task-canceled");
            });
        });
    }

    @LHWorkflow("cancel-user-task-on-deadline")
    public Workflow buildCancelUserTaskOnReassignmentWorkflow() {
        return new WorkflowImpl("cancel-user-task-on-deadline", entrypointThread -> {
            UserTaskOutput formOutput = entrypointThread
                    .assignUserTask(USER_TASK_DEF_NAME, TEST_USER_ID, null)
                    .withOnCancellationException("no-response");
            entrypointThread.cancelUserTaskRunAfter(formOutput, 2);
            entrypointThread.handleException(formOutput, "no-response", userTaskCanceledHandler -> {
                userTaskCanceledHandler.execute("user-task-canceled");
            });
        });
    }

    @LHWorkflow("user-task-assignment")
    public Workflow buildUserTaskAssignmentWorkflow() {
        return new WorkflowImpl("user-task-assignment", wf -> {
            WfRunVariable userId = wf.declareStr("userId");
            WfRunVariable userGroup = wf.declareStr("userGroup");
            wf.assignUserTask(USER_TASK_DEF_NAME, userId, userGroup);
        });
    }

    @LHTaskMethod("my-custom-task")
    public String obiwan(MyForm formData) {
        return "String was " + formData.myStr + " and int was " + formData.myInt;
    }

    @LHTaskMethod("user-task-canceled")
    public String userTaskCanceled() {
        return "User task canceled";
    }

    @LHTaskMethod("reminder-task")
    public void doReminder(WorkerContext ctx) {
        cache.put(ctx.getWfRunId().getId(), "hello there!");
    }

    @LHTaskMethod("verify-worker-context")
    public void verifyWorkerContext(String userId, String userGroup, WorkerContext ctx) {
        if (userId == null && userGroup == null) {
            throw new IllegalStateException("At least one of userId or userGroup must be specified");
        }

        if (userId != null) {
            if (!userId.equals(ctx.getUserId())) {
                throw new IllegalStateException("WorkerContext UserId does not match expected value.");
            }
        }
        if (userGroup != null) {
            if (!userGroup.equals(ctx.getUserGroup())) {
                throw new IllegalStateException("WorkerContext UserGroup does not match expected value.");
            }
        }
    }
}

class MyForm {

    @UserTaskField(displayName = "Str display name", description = "some discription")
    public String myStr;

    @UserTaskField(displayName = "Int display name", description = "another discription")
    public int myInt;

    public MyForm() {}
}
