package io.littlehorse.tests.cases.workflow;

import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.LHPublicApiGrpc.LHPublicApiBlockingStub;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.sdk.worker.WorkerContext;
import io.littlehorse.tests.TestFailure;
import io.littlehorse.tests.WorkflowLogicTest;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class AASequential extends WorkflowLogicTest {

    public AASequential(LHPublicApiBlockingStub client, LHConfig workerConfig) {
        super(client, workerConfig);
    }

    public String getDescription() {
        return "Simple test with workflow that executes two tasks sequentially";
    }

    public Workflow getWorkflowImpl() {
        return new WorkflowImpl(getWorkflowName(), thread -> {
            thread.execute("aa-simple");
            thread.execute("aa-simple");
        });
    }

    public List<Object> getTaskWorkerObjects() {
        return Arrays.asList(new SimpleTask());
    }

    public List<String> launchAndCheckWorkflows(LHPublicApiBlockingStub client)
            throws TestFailure, InterruptedException, IOException {
        String wfRunId = runWf(client);
        Thread.sleep(500);
        assertStatus(client, wfRunId, LHStatus.COMPLETED);

        for (int i = 1; i < 3; i++) {
            assertTaskOutput(client, wfRunId, 0, i, "hello there from wfRun " + wfRunId + " on nodeRun " + i);
        }

        return Arrays.asList(wfRunId);
    }
}

class SimpleTask {

    @LHTaskMethod("aa-simple")
    public String obiWan(WorkerContext context) {
        return ("hello there from wfRun "
                + context.getWfRunId()
                + " on nodeRun "
                + context.getNodeRunId().getPosition());
    }
}
