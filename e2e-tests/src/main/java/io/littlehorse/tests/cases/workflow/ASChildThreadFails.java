package io.littlehorse.tests.cases.workflow;

import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.NodeRun;
import io.littlehorse.sdk.common.proto.WaitForThreadsRun;
import io.littlehorse.sdk.wfsdk.SpawnedThread;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.tests.TestFailure;
import io.littlehorse.tests.WorkflowLogicTest;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class ASChildThreadFails extends WorkflowLogicTest {

    public ASChildThreadFails(LittleHorseBlockingStub client, LHConfig workerConfig) {
        super(client, workerConfig);
    }

    public String getDescription() {
        return "Tests that WAIT_FOR_THREAD throws when child thread dies.";
    }

    private static final String FAILURE_OUTPUT = "this is the failure output";
    private static final String FAILURE_MESSAGE = "this is the failure message";

    public Workflow getWorkflowImpl() {
        return new WorkflowImpl(getWorkflowName(), thread -> {
            SpawnedThread child = thread.spawnThread(
                    subthread -> {
                        subthread.execute("as-obiwan");
                        subthread.fail(FAILURE_OUTPUT, "my-failure", FAILURE_MESSAGE);
                    },
                    "first-thread",
                    null);

            thread.execute("as-obiwan");
            thread.waitForThreads(child);
            thread.execute("as-obiwan");
        });
    }

    public List<Object> getTaskWorkerObjects() {
        return Arrays.asList(new ASSimpleTask());
    }

    public List<String> launchAndCheckWorkflows(LittleHorseBlockingStub client)
            throws TestFailure, InterruptedException, IOException {
        String wfRunId = runWf(client);
        Thread.sleep(500);
        assertStatus(client, wfRunId, LHStatus.EXCEPTION);

        // The parent should only execute one task.
        assertTaskOutputsMatch(client, wfRunId, 0, new ASSimpleTask().obiwan());

        NodeRun nr = getNodeRun(client, wfRunId, 0, 3);

        WaitForThreadsRun wtr = nr.getWaitThreads();
        // There's only one thread in this example, so we only look at the first.
        if (wtr.getThreads(0).getThreadRunNumber() != 1) {
            throw new TestFailure(this, wfRunId + " should have waited for thread 1");
        }

        return Arrays.asList(wfRunId);
    }
}

class ASSimpleTask {

    @LHTaskMethod("as-obiwan")
    public String obiwan() {
        return "hello there";
    }
}
