package io.littlehorse.tests.cases.workflow;

import io.littlehorse.sdk.common.config.LHWorkerConfig;
import io.littlehorse.sdk.common.proto.CancelUserTaskRunRequest;
import io.littlehorse.sdk.common.proto.LHPublicApiGrpc.LHPublicApiBlockingStub;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.NodeRun;
import io.littlehorse.sdk.common.proto.UserTaskRun;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.tests.TestFailure;
import io.littlehorse.tests.UserTaskWorkflowTest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class BEUserTasksCancel extends UserTaskWorkflowTest {

    private static final String USER_TASK_DEF_NAME = "be-some-usertask";

    public BEUserTasksCancel(LHPublicApiBlockingStub client, LHWorkerConfig workerConfig) {
        super(client, workerConfig);
    }

    public String getDescription() {
        return ("Test for cancelling a UserTaskRun");
    }

    @Override
    public Map<String, Object> getRequiredUserTaskForms() {
        return Map.of(USER_TASK_DEF_NAME, new AZUserTaskForm());
    }

    @Override
    public Workflow getWorkflowImpl() {
        return new WorkflowImpl(getWorkflowName(), thread -> {
            WfRunVariable formVar = thread.addVariable("form", VariableType.JSON_OBJ);

            thread.assignUserTaskToUser(USER_TASK_DEF_NAME, "test-user");

            thread.execute("be-unreachable-task", formVar);
        });
    }

    @Override
    public List<Object> getTaskWorkerObjects() {
        return Arrays.asList(new BECancelTask());
    }

    public List<String> launchAndCheckWorkflows(LHPublicApiBlockingStub client)
            throws TestFailure, InterruptedException, IOException {
        List<String> out = new ArrayList<>();

        String wfRunId = runWf(client);
        Thread.sleep(8 * 1000); // Wait for reminder task to execute

        // Get the UserTaskRun, ensure that there is an event with a taskRunId
        NodeRun firstUserTask = getNodeRun(client, wfRunId, 0, 1);
        UserTaskRun utr = getUserTaskRun(client, firstUserTask.getUserTask().getUserTaskRunId());
        CancelUserTaskRunRequest cancelUserTaskRun = CancelUserTaskRunRequest.newBuilder()
                .setUserTaskRunId(utr.getId())
                .build();
        client.cancelUserTaskRun(cancelUserTaskRun);
        assertStatus(client, wfRunId, LHStatus.ERROR);
        return out;
    }
}

class BECancelTask {

    @LHTaskMethod("be-task-cancelled")
    public String obiwan(AZUserTaskForm formData) {
        return "String was " + formData.myStr + " and int was " + formData.myInt;
    }

    @LHTaskMethod("be-unreachable-task")
    public String unreachable(AZUserTaskForm formData) {
        return "Nothing to do...";
    }
}
