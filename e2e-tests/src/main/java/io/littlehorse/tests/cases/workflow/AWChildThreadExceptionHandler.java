package io.littlehorse.tests.cases.workflow;

import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.WaitForThreadsPolicy;
import io.littlehorse.sdk.wfsdk.NodeOutput;
import io.littlehorse.sdk.wfsdk.SpawnedThread;
import io.littlehorse.sdk.wfsdk.SpawnedThreads;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.tests.TestFailure;
import io.littlehorse.tests.WorkflowLogicTest;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class AWChildThreadExceptionHandler extends WorkflowLogicTest {

    public AWChildThreadExceptionHandler(LittleHorseBlockingStub client, LHConfig workerConfig) {
        super(client, workerConfig);
    }

    public String getDescription() {
        return ("Tests that we can put an exception handler on WAIT_FOR_THREAD node"
                + " in case the child thread fails.");
    }

    public Workflow getWorkflowImpl() {
        return new WorkflowImpl(getWorkflowName(), thread -> {
            SpawnedThread childThread = thread.spawnThread(
                    child -> {
                        child.execute("aw-fail");
                    },
                    "child",
                    null);

            NodeOutput toHandle = thread.waitForThreads(SpawnedThreads.of(childThread))
                    .withPolicy(WaitForThreadsPolicy.STOP_ON_FAILURE);

            thread.handleError(toHandle, handler -> {
                handler.execute("aw-echo", "hi from handler");
            });
            thread.execute("aw-succeed");
        });
    }

    public List<Object> getTaskWorkerObjects() {
        return Arrays.asList(new AWSimpleTask());
    }

    public List<String> launchAndCheckWorkflows(LittleHorseBlockingStub client)
            throws TestFailure, InterruptedException, IOException {
        String wfRunId = runWf(client);

        Thread.sleep(500);
        assertStatus(client, wfRunId, LHStatus.COMPLETED);
        assertThreadStatus(client, wfRunId, 0, LHStatus.COMPLETED);
        assertThreadStatus(client, wfRunId, 1, LHStatus.ERROR);
        assertThreadStatus(client, wfRunId, 2, LHStatus.COMPLETED);
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
