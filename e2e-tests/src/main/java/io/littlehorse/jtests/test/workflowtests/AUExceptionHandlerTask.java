package io.littlehorse.jtests.test.workflowtests;

import io.littlehorse.jlib.client.LHClient;
import io.littlehorse.jlib.common.config.LHWorkerConfig;
import io.littlehorse.jlib.common.exception.LHApiError;
import io.littlehorse.jlib.common.proto.LHStatusPb;
import io.littlehorse.jlib.wfsdk.NodeOutput;
import io.littlehorse.jlib.wfsdk.Workflow;
import io.littlehorse.jlib.wfsdk.internal.WorkflowImpl;
import io.littlehorse.jlib.worker.LHTaskMethod;
import io.littlehorse.jtests.test.LogicTestFailure;
import io.littlehorse.jtests.test.WorkflowLogicTest;
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
