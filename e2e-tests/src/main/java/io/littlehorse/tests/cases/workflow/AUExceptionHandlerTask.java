package io.littlehorse.tests.cases.workflow;

import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.wfsdk.NodeOutput;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.tests.TestFailure;
import io.littlehorse.tests.WorkflowLogicTest;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class AUExceptionHandlerTask extends WorkflowLogicTest {

    public AUExceptionHandlerTask(LittleHorseBlockingStub client, LHConfig workerConfig) {
        super(client, workerConfig);
    }

    public String getDescription() {
        return "Tests basic exception handler use case.";
    }

    public Workflow getWorkflowImpl() {
        return new WorkflowImpl(getWorkflowName(), thread -> {
            NodeOutput taskThatWillFail = thread.execute("au-will-fail");
            thread.handleError(taskThatWillFail, handler -> {
                handler.execute("au-obiwan");
            });
        });
    }

    public List<Object> getTaskWorkerObjects() {
        return Arrays.asList(new AUSimpleTask());
    }

    public List<String> launchAndCheckWorkflows(LittleHorseBlockingStub client)
            throws TestFailure, InterruptedException, IOException {
        String wfRunId = runWf(client);
        Thread.sleep(500);
        assertStatus(client, wfRunId, LHStatus.COMPLETED);

        // Check that the handler ran.
        assertTaskOutputsMatch(client, wfRunId, 1, new AUSimpleTask().obiwan());
        return Arrays.asList(wfRunId);
    }
}

class AUSimpleTask {

    @LHTaskMethod("au-obiwan")
    public String obiwan() {
        return "hello there";
    }

    @LHTaskMethod("au-will-fail")
    public String willFail() {
        throw new RuntimeException("Yikes");
    }
}
