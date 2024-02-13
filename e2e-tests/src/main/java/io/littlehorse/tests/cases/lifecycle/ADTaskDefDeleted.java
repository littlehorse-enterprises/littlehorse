package io.littlehorse.tests.cases.lifecycle;

import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.DeleteTaskDefRequest;
import io.littlehorse.sdk.common.proto.DeleteWfRunRequest;
import io.littlehorse.sdk.common.proto.DeleteWfSpecRequest;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.RunWfRequest;
import io.littlehorse.sdk.common.proto.TaskDefId;
import io.littlehorse.sdk.common.proto.WfRun;
import io.littlehorse.sdk.common.proto.WfRunId;
import io.littlehorse.sdk.common.proto.WfSpecId;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.sdk.worker.LHTaskWorker;
import io.littlehorse.tests.Test;

/*
 * This test involves deploying a WfSpec, then deleting a TaskDef, then
 * running the WfRun. The NodeRun should fail with the.
 *
 * 1. Re-create the behavior where the LH Server crashes
 * 2. Add a safeguard to CommandProcessor:process() which prevents the crash
 * 3. Verify the crash is fixed
 * 4. Make WfRun fail gracefully if TaskDef is deleted.
 */
public class ADTaskDefDeleted extends Test {

    public static final String TASK_DEF_1 = "ad-lifecycle-taskdef1";
    public static final String TASK_DEF_2 = "ad-lifecycle-taskdef2";
    public static final String WF_SPEC_NAME = "ad-taskdef-deleted";
    private String wfRunId;
    private LHTaskWorker worker1;
    private LHTaskWorker worker2;

    public ADTaskDefDeleted(LittleHorseBlockingStub client, LHConfig config) {
        super(client, config);
    }

    public String getDescription() {
        return """
Tests that when we run a WfRun after deleting one of the necessary TaskDef's:
1. The server doesn't crash
2. The command doesn't hang
3. The WfRun is marked as 'ERROR'.
                """;
    }

    public void test() throws InterruptedException {
        worker1 = new LHTaskWorker(new TaskWfSpecLifecycleWorker(), TASK_DEF_1, workerConfig);
        worker1.registerTaskDef();
        worker2 = new LHTaskWorker(new TaskWfSpecLifecycleWorker(), TASK_DEF_2, workerConfig);
        worker2.registerTaskDef();

        new WorkflowImpl(WF_SPEC_NAME, thread -> {
                    thread.execute(TASK_DEF_1);
                    thread.execute(TASK_DEF_2);
                })
                .registerWfSpec(client);

        Thread.sleep(200); // Wait for the data to propagate
        worker1.start();
        worker2.start();

        // Delete the TaskDef
        client.deleteTaskDef(DeleteTaskDefRequest.newBuilder()
                .setId(TaskDefId.newBuilder().setName(TASK_DEF_2))
                .build());

        Thread.sleep(120);

        wfRunId = client.runWf(
                        RunWfRequest.newBuilder().setWfSpecName(WF_SPEC_NAME).build())
                .getId()
                .getId();

        Thread.sleep(120);
        WfRun wfRun = client.getWfRun(WfRunId.newBuilder().setId(wfRunId).build());
        if (wfRun.getStatus() != LHStatus.ERROR) {
            throw new RuntimeException("Should have failed!");
        }

        if (!wfRun.getThreadRuns(0).getErrorMessage().contains("Appears that TaskDef was deleted")) {
            throw new RuntimeException("Should have error message about deleted taskdef! WfRun: " + wfRunId);
        }
    }

    public void cleanup() {
        try {
            client.deleteWfRun(DeleteWfRunRequest.newBuilder()
                    .setId(WfRunId.newBuilder().setId(wfRunId))
                    .build());
            client.deleteWfSpec(DeleteWfSpecRequest.newBuilder()
                    .setId(WfSpecId.newBuilder().setName(WF_SPEC_NAME))
                    .build());
            client.deleteTaskDef(DeleteTaskDefRequest.newBuilder()
                    .setId(TaskDefId.newBuilder().setName(TASK_DEF_1))
                    .build());
            worker1.close();
            worker2.close();
            client.deleteTaskDef(DeleteTaskDefRequest.newBuilder()
                    .setId(TaskDefId.newBuilder().setName(TASK_DEF_2))
                    .build());
        } catch (Exception exn) {
        }
    }
}

class TaskWfSpecLifecycleWorker {

    @LHTaskMethod(ADTaskDefDeleted.TASK_DEF_1)
    public String foo() {
        return "hi";
    }

    @LHTaskMethod(ADTaskDefDeleted.TASK_DEF_2)
    public String bar() {
        return "hi";
    }
}
