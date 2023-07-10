package io.littlehorse.jtests.test.workflowtests;

import io.littlehorse.jtests.test.LogicTestFailure;
import io.littlehorse.jtests.test.WorkflowLogicTest;
import io.littlehorse.sdk.client.LHClient;
import io.littlehorse.sdk.common.config.LHWorkerConfig;
import io.littlehorse.sdk.common.exception.LHApiError;
import io.littlehorse.sdk.common.proto.LHStatusPb;
import io.littlehorse.sdk.wfsdk.NodeOutput;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskMethod;
import java.util.Arrays;
import java.util.List;

public class AUExceptionHandlerTask extends WorkflowLogicTest {

    public AUExceptionHandlerTask(LHClient client, LHWorkerConfig workerConfig) {
        super(client, workerConfig);
    }

    public String getDescription() {
        return "Tests basic exception handler use case.";
    }

    public Workflow getWorkflowImpl() {
        return new WorkflowImpl(
            getWorkflowName(),
            thread -> {
                NodeOutput taskThatWillFail = thread.execute("au-will-fail");
                thread.handleException(
                    taskThatWillFail,
                    null,
                    handler -> {
                        handler.execute("au-obiwan");
                    }
                );
            }
        );
    }

    public List<Object> getTaskWorkerObjects() {
        return Arrays.asList(new AUSimpleTask());
    }

    public List<String> launchAndCheckWorkflows(LHClient client)
        throws LogicTestFailure, InterruptedException, LHApiError {
        String wfRunId = runWf(client);
        Thread.sleep(300);
        assertStatus(client, wfRunId, LHStatusPb.COMPLETED);

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
