package io.littlehorse.tests.cases.lifecycle;

import io.littlehorse.sdk.client.LHClient;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.config.LHWorkerConfig;
import io.littlehorse.sdk.common.exception.LHApiError;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.AssignUserTaskRunPb;
import io.littlehorse.sdk.common.proto.AssignUserTaskRunReplyPb;
import io.littlehorse.sdk.common.proto.CompleteUserTaskRunPb;
import io.littlehorse.sdk.common.proto.LHResponseCodePb;
import io.littlehorse.sdk.common.proto.LHStatusPb;
import io.littlehorse.sdk.common.proto.SearchTaskRunReplyPb;
import io.littlehorse.sdk.common.proto.SearchUserTaskRunReplyPb;
import io.littlehorse.sdk.common.proto.TaskRunIdPb;
import io.littlehorse.sdk.common.proto.TaskStatusPb;
import io.littlehorse.sdk.common.proto.UserTaskFieldResultPb;
import io.littlehorse.sdk.common.proto.UserTaskResultPb;
import io.littlehorse.sdk.common.proto.UserTaskRunIdPb;
import io.littlehorse.sdk.common.proto.UserTaskRunStatusPb;
import io.littlehorse.sdk.common.proto.VariableTypePb;
import io.littlehorse.sdk.common.proto.WfRunIdPb;
import io.littlehorse.sdk.common.proto.WfSpecPb;
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
            VariableTypePb.BOOL
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

        WfSpecPb result = client.putWfSpec(
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
        List<WfRunIdPb> runningWfs = client.searchWfRun(
            WF_SPEC_NAME,
            wfSpecVersion,
            LHStatusPb.RUNNING,
            new Date(System.currentTimeMillis() - 5000),
            new Date()
        );
        assertContainsWfRun(runningWfs, succeedWf);
        assertContainsWfRun(runningWfs, failWf);

        failWorker.start();
        Thread.sleep(800); // wait for worker to do its thing

        assertStatus(client, succeedWf, LHStatusPb.RUNNING);
        assertStatus(client, failWf, LHStatusPb.ERROR);
        SearchTaskRunReplyPb failedTasks = searchTaskRuns(
            FAIL_TASK,
            TaskStatusPb.TASK_FAILED
        );
        assertContainsWfRun(failedTasks, failWf);
        assertNotContainsWfRun(failedTasks, succeedWf);

        SearchTaskRunReplyPb runningFailTasks = searchTaskRuns(
            FAIL_TASK,
            TaskStatusPb.TASK_RUNNING
        );
        assertNotContainsWfRun(runningFailTasks, failWf);
        assertNotContainsWfRun(runningFailTasks, succeedWf);
        SearchTaskRunReplyPb runningSlowTasks = searchTaskRuns(
            SLOW_TASK,
            TaskStatusPb.TASK_RUNNING
        );
        assertContainsWfRun(runningSlowTasks, succeedWf);
        assertNotContainsWfRun(runningSlowTasks, failWf);
        Thread.sleep(1100);
        runningSlowTasks = searchTaskRuns(SLOW_TASK, TaskStatusPb.TASK_RUNNING);
        assertNotContainsWfRun(runningSlowTasks, succeedWf);
        assertNotContainsWfRun(runningSlowTasks, failWf);

        SearchTaskRunReplyPb succeededSlowTasks = searchTaskRuns(
            SLOW_TASK,
            TaskStatusPb.TASK_SUCCESS
        );
        assertContainsWfRun(succeededSlowTasks, succeedWf);

        // Ok, now we look for the UserTaskRuns
        assertContainsWfRun(searchUserTaskRunsUserId("obiwan"), succeedWf);
        assertContainsWfRun(
            searchUserTaskRunsUserId("obiwan", UserTaskRunStatusPb.CLAIMED),
            succeedWf
        );
        assertContainsWfRun(
            searchUserTaskRunsUserId(
                "obiwan",
                USER_TASK,
                UserTaskRunStatusPb.CLAIMED
            ),
            succeedWf
        );

        UserTaskRunIdPb userTaskId = searchUserTaskRunsUserId("obiwan")
            .getResultsList()
            .stream()
            .filter(id -> id.getWfRunId().equals(succeedWf))
            .findFirst()
            .orElseThrow();

        AssignUserTaskRunReplyPb assignReply = client
            .getGrpcClient()
            .assignUserTaskRun(
                AssignUserTaskRunPb
                    .newBuilder()
                    .setUserId("fdsa")
                    .setOverrideClaim(false)
                    .setUserTaskRunId(userTaskId)
                    .build()
            );
        assertThat(
            assignReply.getCode() == LHResponseCodePb.ALREADY_EXISTS_ERROR,
            "should be unable to reassign without override claim"
        );
        assignReply =
            client
                .getGrpcClient()
                .assignUserTaskRun(
                    AssignUserTaskRunPb
                        .newBuilder()
                        .setUserId("fdsa")
                        .setOverrideClaim(true)
                        .setUserTaskRunId(userTaskId)
                        .build()
                );
        assertThat(
            assignReply.getCode() == LHResponseCodePb.OK,
            "should be able to reassign with override claim"
        );
        Thread.sleep(150); // allow remote tag to propagate

        // Shouldn't be obiwan's task anymore
        assertNotContainsWfRun(
            searchUserTaskRunsUserId("obiwan", UserTaskRunStatusPb.CLAIMED),
            succeedWf
        );
        assertContainsWfRun(
            searchUserTaskRunsUserId("fdsa", UserTaskRunStatusPb.CLAIMED),
            succeedWf
        );

        AssignUserTaskRunReplyPb reAssignReply = client
            .getGrpcClient()
            .assignUserTaskRun(
                AssignUserTaskRunPb
                    .newBuilder()
                    .setUserGroup("mygroup")
                    .setOverrideClaim(true)
                    .setUserTaskRunId(userTaskId)
                    .build()
            );
        System.out.println(LHLibUtil.protoToJson(reAssignReply));
        assertThat(
            reAssignReply.getCode() == LHResponseCodePb.OK,
            "should be able to reassign"
        );
        Thread.sleep(150); //allow remote indexes to propagate
        assertContainsWfRun(
            searchUserTaskRunsUserGroup(
                "mygroup",
                UserTaskRunStatusPb.ASSIGNED_NOT_CLAIMED
            ),
            succeedWf
        );
        assertNotContainsWfRun(
            searchUserTaskRunsUserId("fdsa", UserTaskRunStatusPb.CLAIMED),
            succeedWf
        );

        // Now we claim it once more
        client
            .getGrpcClient()
            .assignUserTaskRun(
                AssignUserTaskRunPb
                    .newBuilder()
                    .setUserId("yoda")
                    .setOverrideClaim(true)
                    .setUserTaskRunId(userTaskId)
                    .build()
            );
        Thread.sleep(150); // allow remote tag to propagate

        assertContainsWfRun(searchUserTaskRunsUserId("yoda"), succeedWf);
        assertContainsWfRun(
            searchUserTaskRunsUserId("yoda", UserTaskRunStatusPb.CLAIMED),
            succeedWf
        );
        assertNotContainsWfRun(
            searchUserTaskRunsUserGroup(
                "mygroup",
                UserTaskRunStatusPb.ASSIGNED_NOT_CLAIMED
            ),
            succeedWf
        );

        // Finally, complete the TaskRun
        client
            .getGrpcClient()
            .completeUserTaskRun(
                CompleteUserTaskRunPb
                    .newBuilder()
                    .setUserTaskRunId(userTaskId)
                    .setUserId("yoda")
                    .setResult(
                        UserTaskResultPb
                            .newBuilder()
                            .addFields(
                                UserTaskFieldResultPb
                                    .newBuilder()
                                    .setName("foo")
                                    .setValue(LHLibUtil.objToVarVal("bar"))
                            )
                    )
                    .build()
            );
        Thread.sleep(150);

        assertContainsWfRun(
            searchUserTaskRunsUserId("yoda", UserTaskRunStatusPb.DONE),
            succeedWf
        );

        assertStatus(client, succeedWf, LHStatusPb.COMPLETED);
    }

    private void assertContainsWfRun(List<WfRunIdPb> wfRuns, String id) {
        for (WfRunIdPb wfRunId : wfRuns) {
            if (id.equals(wfRunId.getId())) {
                return;
            }
        }
        throw new RuntimeException("Should have found WfRun " + id);
    }

    private void assertContainsWfRun(SearchUserTaskRunReplyPb results, String id) {
        for (UserTaskRunIdPb trid : results.getResultsList()) {
            if (trid.getWfRunId().equals(id)) {
                return;
            }
        }
        throw new RuntimeException("Should have found WfRun " + id);
    }

    private void assertNotContainsWfRun(SearchUserTaskRunReplyPb results, String id) {
        for (UserTaskRunIdPb trid : results.getResultsList()) {
            if (trid.getWfRunId().equals(id)) {
                throw new RuntimeException("Should NOT have found WfRun " + id);
            }
        }
    }

    private void assertNotContainsWfRun(SearchTaskRunReplyPb results, String id) {
        for (TaskRunIdPb trid : results.getResultsList()) {
            if (trid.getWfRunId().equals(id)) {
                throw new RuntimeException("Should NOT have found WfRun " + id);
            }
        }
    }

    private void assertContainsWfRun(SearchTaskRunReplyPb results, String id) {
        for (TaskRunIdPb trid : results.getResultsList()) {
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
