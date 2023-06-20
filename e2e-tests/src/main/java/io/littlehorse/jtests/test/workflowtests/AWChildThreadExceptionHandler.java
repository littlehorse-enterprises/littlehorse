package io.littlehorse.jtests.test.workflowtests;

import io.littlehorse.jlib.client.LHClient;
import io.littlehorse.jlib.common.config.LHWorkerConfig;
import io.littlehorse.jlib.common.exception.LHApiError;
import io.littlehorse.jlib.common.proto.LHStatusPb;
import io.littlehorse.jlib.wfsdk.NodeOutput;
import io.littlehorse.jlib.wfsdk.SpawnedThread;
import io.littlehorse.jlib.wfsdk.Workflow;
import io.littlehorse.jlib.wfsdk.internal.WorkflowImpl;
import io.littlehorse.jlib.worker.LHTaskMethod;
import io.littlehorse.jtests.test.LogicTestFailure;
import io.littlehorse.jtests.test.WorkflowLogicTest;
import java.util.Arrays;
import java.util.List;

public class AWChildThreadExceptionHandler extends WorkflowLogicTest {

    public AWChildThreadExceptionHandler(
        LHClient client,
        LHWorkerConfig workerConfig
    ) {
        super(client, workerConfig);
    }

    public String getDescription() {
        return (
            "Tests that we can put an exception handler on WAIT_FOR_THREAD node" +
            " in case the child thread fails."
        );
    }

    public Workflow getWorkflowImpl() {
        return new WorkflowImpl(
            getWorkflowName(),
            thread -> {
                SpawnedThread childThread = thread.spawnThread(
                    child -> {
                        child.execute("aw-fail");
                    },
                    "child",
                    null
                );

                NodeOutput toHandle = thread.waitForThread(childThread);
                thread.handleException(
                    toHandle,
                    null,
                    handler -> {
                        handler.execute("aw-echo", "hi from handler");
                    }
                );
                thread.execute("aw-succeed");
            }
        );
    }

    public List<Object> getTaskWorkerObjects() {
        return Arrays.asList(new AWSimpleTask());
    }

    public List<String> launchAndCheckWorkflows(LHClient client)
        throws LogicTestFailure, InterruptedException, LHApiError {
        String wfRunId = runWf(client);

        Thread.sleep(500);
        assertStatus(client, wfRunId, LHStatusPb.COMPLETED);
        assertThreadStatus(client, wfRunId, 0, LHStatusPb.COMPLETED);
        assertThreadStatus(client, wfRunId, 1, LHStatusPb.ERROR);
        assertThreadStatus(client, wfRunId, 2, LHStatusPb.COMPLETED);
        assertTaskOutputsMatch(client, wfRunId, 2, "hi from handler");
        assertTaskOutputsMatch(client, wfRunId, 0, "Success!");

        return Arrays.asList(wfRunId);
    }
}

class AWSimpleTask {

    @LHTaskMethod("aw-fail")
    public String fail() {
        throw new RuntimeException("ooph");
    }

    @LHTaskMethod("aw-echo")
    public String echo(String input) {
        return input;
    }

    @LHTaskMethod("aw-succeed")
    public String succeed() {
        return "Success!";
    }
}
