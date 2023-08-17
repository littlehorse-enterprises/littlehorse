package io.littlehorse.tests.cases.lifecycle;

import io.littlehorse.sdk.client.LHClient;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.config.LHWorkerConfig;
import io.littlehorse.sdk.common.exception.LHApiError;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.AssignUserTaskRunRequest;
import io.littlehorse.sdk.common.proto.AssignUserTaskRunResponse;
import io.littlehorse.sdk.common.proto.CompleteUserTaskRunRequest;
import io.littlehorse.sdk.common.proto.LHResponseCode;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.SearchTaskRunResponse;
import io.littlehorse.sdk.common.proto.SearchUserTaskRunResponse;
import io.littlehorse.sdk.common.proto.TaskRunId;
import io.littlehorse.sdk.common.proto.TaskStatus;
import io.littlehorse.sdk.common.proto.User;
import io.littlehorse.sdk.common.proto.UserGroup;
import io.littlehorse.sdk.common.proto.UserTaskFieldResult;
import io.littlehorse.sdk.common.proto.UserTaskResult;
import io.littlehorse.sdk.common.proto.UserTaskRunId;
import io.littlehorse.sdk.common.proto.UserTaskRunStatus;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.WfRunId;
import io.littlehorse.sdk.common.proto.WfSpec;
import io.littlehorse.sdk.common.util.Arg;
import io.littlehorse.sdk.usertask.UserTaskSchema;
import io.littlehorse.sdk.usertask.annotations.UserTaskField;
import io.littlehorse.sdk.wfsdk.ThreadBuilder;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.sdk.worker.LHTaskWorker;
import io.littlehorse.tests.Test;
import io.littlehorse.tests.TestFailure;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AFTaskAndUserTaskSearch extends Test {

    public static final String USER_TASK = "af-user-task";
    public static final String SLOW_TASK = "af-slow-task";
    public static final String FAIL_TASK = "af-fail-if-true";
    public static final String WF_SPEC_NAME = "af-wf";

    private int wfSpecVersion;
    private LHTaskWorker failWorker;
    private LHTaskWorker slowWorker;
    private List<String> wfRunIds;

    public String getDescription() {
        return """
Tests various aspects of TaskRun and UserTaskRun searc:
- Search for RUNNING WfRun's (including pagination)
- Search for STARTING TaskRuns
- Search for COMPLETED and ERROR TaskRuns
- Search for UserTaskRun by userGroup
- Search for UserTaskRun by userId
- Reassign UserTaskRun
- Search for UserTaskRun where status is DONE
                """;
    }

    public void wf(ThreadBuilder thread) {
        WfRunVariable shouldFail = thread.addVariable(
            "should-fail",
            VariableType.BOOL
        );

        thread.execute(FAIL_TASK, shouldFail);
        thread.execute(SLOW_TASK);

        thread.assignUserTaskToUser(USER_TASK, "obiwan");
    }

    public AFTaskAndUserTaskSearch(LHClient client, LHWorkerConfig config) {
        super(client, config);
    }

    public void test()
        throws LHApiError, InterruptedException, TestFailure, LHSerdeError {
        wfRunIds = new ArrayList<>();
        UserTaskSchema uts = new UserTaskSchema(new UserTaskForm(), USER_TASK);
        client.putUserTaskDef(uts.compile(), true);
        failWorker = new LHTaskWorker(new AFSearchWorker(), FAIL_TASK, workerConfig);
        slowWorker = new LHTaskWorker(new AFSearchWorker(), SLOW_TASK, workerConfig);
        failWorker.registerTaskDef(true);
        slowWorker.registerTaskDef(true);
        slowWorker.start();
        // will start the failWorker later so we can catch the STARTING tasks

        WfSpec result = client.putWfSpec(
            new WorkflowImpl(WF_SPEC_NAME, this::wf).compileWorkflow()
        );
        wfSpecVersion = result.getVersion();

        Thread.sleep(150);

        String failWf = client.runWf(
            WF_SPEC_NAME,
            wfSpecVersion,
            null,
            Arg.of("should-fail", true)
        );
        String succeedWf = client.runWf(
            WF_SPEC_NAME,
            wfSpecVersion,
            null,
            Arg.of("should-fail", false)
        );
        wfRunIds.add(failWf);
        wfRunIds.add(succeedWf);

        assertVarEqual(client, succeedWf, 0, "should-fail", false);
        assertVarEqual(client, failWf, 0, "should-fail", true);

        // Since we haven't started the first TaskWorker, the tasks should both
        // be STARTING. We'll test out pagination.
        List<WfRunId> runningWfs = client.searchWfRun(
            WF_SPEC_NAME,
            wfSpecVersion,
            LHStatus.RUNNING,
            new Date(System.currentTimeMillis() - 5000),
            new Date()
        );
        assertContainsWfRun(runningWfs, succeedWf);
        assertContainsWfRun(runningWfs, failWf);

        failWorker.start();
        Thread.sleep(800); // wait for worker to do its thing

        assertStatus(client, succeedWf, LHStatus.RUNNING);
        assertStatus(client, failWf, LHStatus.ERROR);
        SearchTaskRunResponse failedTasks = searchTaskRuns(
            FAIL_TASK,
            TaskStatus.TASK_FAILED
        );
        assertContainsWfRun(failedTasks, failWf);
        assertNotContainsWfRun(failedTasks, succeedWf);

        SearchTaskRunResponse runningFailTasks = searchTaskRuns(
            FAIL_TASK,
            TaskStatus.TASK_RUNNING
        );
        assertNotContainsWfRun(runningFailTasks, failWf);
        assertNotContainsWfRun(runningFailTasks, succeedWf);
        SearchTaskRunResponse runningSlowTasks = searchTaskRuns(
            SLOW_TASK,
            TaskStatus.TASK_RUNNING
        );
        assertContainsWfRun(runningSlowTasks, succeedWf);
        assertNotContainsWfRun(runningSlowTasks, failWf);
        Thread.sleep(1100);
        runningSlowTasks = searchTaskRuns(SLOW_TASK, TaskStatus.TASK_RUNNING);
        assertNotContainsWfRun(runningSlowTasks, succeedWf);
        assertNotContainsWfRun(runningSlowTasks, failWf);

        SearchTaskRunResponse succeededSlowTasks = searchTaskRuns(
            SLOW_TASK,
            TaskStatus.TASK_SUCCESS
        );
        assertContainsWfRun(succeededSlowTasks, succeedWf);

        // Ok, now we look for the UserTaskRuns
        assertContainsWfRun(searchUserTaskRunsUserId("obiwan"), succeedWf);
        assertContainsWfRun(
            searchUserTaskRunsUserId("obiwan", UserTaskRunStatus.ASSIGNED),
            succeedWf
        );
        assertContainsWfRun(
            searchUserTaskRunsUserId("obiwan", USER_TASK, UserTaskRunStatus.ASSIGNED),
            succeedWf
        );

        UserTaskRunId userTaskId = searchUserTaskRunsUserId("obiwan")
            .getResultsList()
            .stream()
            .filter(id -> id.getWfRunId().equals(succeedWf))
            .findFirst()
            .orElseThrow();

        AssignUserTaskRunResponse assignReply = client
            .getGrpcClient()
            .assignUserTaskRun(
                AssignUserTaskRunRequest
                    .newBuilder()
                    .setUser(User.newBuilder().setId("fdsa").build())
                    .setOverrideClaim(false)
                    .setUserTaskRunId(userTaskId)
                    .build()
            );
        assertThat(
            assignReply.getCode() == LHResponseCode.ALREADY_EXISTS_ERROR,
            "should be unable to reassign without override claim"
        );
        assignReply =
            client
                .getGrpcClient()
                .assignUserTaskRun(
                    AssignUserTaskRunRequest
                        .newBuilder()
                        .setUser(User.newBuilder().setId("fdsa").build())
                        .setOverrideClaim(true)
                        .setUserTaskRunId(userTaskId)
                        .build()
                );
        assertThat(
            assignReply.getCode() == LHResponseCode.OK,
            "should be able to reassign with override claim"
        );
        Thread.sleep(150); // allow remote tag to propagate

        // Shouldn't be obiwan's task anymore
        assertNotContainsWfRun(
            searchUserTaskRunsUserId("obiwan", UserTaskRunStatus.ASSIGNED),
            succeedWf
        );
        assertContainsWfRun(
            searchUserTaskRunsUserId("fdsa", UserTaskRunStatus.ASSIGNED),
            succeedWf
        );

        AssignUserTaskRunResponse reAssignReply = client
            .getGrpcClient()
            .assignUserTaskRun(
                AssignUserTaskRunRequest
                    .newBuilder()
                    .setUserGroup(UserGroup.newBuilder().setId("mygroup").build())
                    .setOverrideClaim(true)
                    .setUserTaskRunId(userTaskId)
                    .build()
            );
        System.out.println(LHLibUtil.protoToJson(reAssignReply));
        assertThat(
            reAssignReply.getCode() == LHResponseCode.OK,
            "should be able to reassign"
        );
        Thread.sleep(150); //allow remote indexes to propagate
        assertContainsWfRun(
            searchUserTaskRunsUserGroup("mygroup", UserTaskRunStatus.UNASSIGNED),
            succeedWf
        );
        assertNotContainsWfRun(
            searchUserTaskRunsUserId("fdsa", UserTaskRunStatus.ASSIGNED),
            succeedWf
        );

        // Now we claim it once more
        client
            .getGrpcClient()
            .assignUserTaskRun(
                AssignUserTaskRunRequest
                    .newBuilder()
                    .setUser(User.newBuilder().setId("yoda").build())
                    .setOverrideClaim(true)
                    .setUserTaskRunId(userTaskId)
                    .build()
            );
        Thread.sleep(150); // allow remote tag to propagate

        assertContainsWfRun(searchUserTaskRunsUserId("yoda"), succeedWf);
        assertContainsWfRun(
            searchUserTaskRunsUserId("yoda", UserTaskRunStatus.ASSIGNED),
            succeedWf
        );
        assertNotContainsWfRun(
            searchUserTaskRunsUserGroup("mygroup", UserTaskRunStatus.UNASSIGNED),
            succeedWf
        );

        // Finally, complete the TaskRun
        client
            .getGrpcClient()
            .completeUserTaskRun(
                CompleteUserTaskRunRequest
                    .newBuilder()
                    .setUserTaskRunId(userTaskId)
                    .setUserId("yoda")
                    .setResult(
                        UserTaskResult
                            .newBuilder()
                            .addFields(
                                UserTaskFieldResult
                                    .newBuilder()
                                    .setName("foo")
                                    .setValue(LHLibUtil.objToVarVal("bar"))
                            )
                    )
                    .build()
            );
        Thread.sleep(150);

        assertContainsWfRun(
            searchUserTaskRunsUserId("yoda", UserTaskRunStatus.DONE),
            succeedWf
        );

        assertStatus(client, succeedWf, LHStatus.COMPLETED);
    }

    private void assertContainsWfRun(List<WfRunId> wfRuns, String id) {
        for (WfRunId wfRunId : wfRuns) {
            if (id.equals(wfRunId.getId())) {
                return;
            }
        }
        throw new RuntimeException("Should have found WfRun " + id);
    }

    private void assertContainsWfRun(SearchUserTaskRunResponse results, String id) {
        for (UserTaskRunId trid : results.getResultsList()) {
            if (trid.getWfRunId().equals(id)) {
                return;
            }
        }
        throw new RuntimeException("Should have found WfRun " + id);
    }

    private void assertNotContainsWfRun(
        SearchUserTaskRunResponse results,
        String id
    ) {
        for (UserTaskRunId trid : results.getResultsList()) {
            if (trid.getWfRunId().equals(id)) {
                throw new RuntimeException("Should NOT have found WfRun " + id);
            }
        }
    }

    private void assertNotContainsWfRun(SearchTaskRunResponse results, String id) {
        for (TaskRunId trid : results.getResultsList()) {
            if (trid.getWfRunId().equals(id)) {
                throw new RuntimeException("Should NOT have found WfRun " + id);
            }
        }
    }

    private void assertContainsWfRun(SearchTaskRunResponse results, String id) {
        for (TaskRunId trid : results.getResultsList()) {
            if (trid.getWfRunId().equals(id)) {
                return;
            }
        }
        throw new RuntimeException("Should NOT have found WfRun " + id);
    }

    public void cleanup() throws LHApiError {
        // for (String wfRunId : wfRunIds) {
        //     client.deleteWfRun(wfRunId);
        // }
        // client.deleteTaskDef(FAIL_TASK);
        // client.deleteTaskDef(SLOW_TASK);
        // client.deleteWfSpec(WF_SPEC_NAME, wfSpecVersion);
    }
}

class AFSearchWorker {

    @LHTaskMethod("af-slow-task")
    public String slow() throws Exception {
        Thread.sleep(1000);
        return "slow task";
    }

    @LHTaskMethod("af-fail-if-true")
    public String failIfTrue(boolean shouldFail) {
        if (shouldFail) {
            throw new RuntimeException("yikes asdf");
        }

        return "haha, didn't fail!";
    }
}

class UserTaskForm {

    @UserTaskField
    public String foo;
}
