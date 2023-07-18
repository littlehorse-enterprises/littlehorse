package io.littlehorse.tests.cases.workflow;

import io.littlehorse.sdk.client.LHClient;
import io.littlehorse.sdk.common.config.LHWorkerConfig;
import io.littlehorse.sdk.common.exception.LHApiError;
import io.littlehorse.sdk.common.proto.LHStatusPb;
import io.littlehorse.sdk.common.proto.NodeRunPb;
import io.littlehorse.sdk.common.proto.WaitForThreadsRunPb;
import io.littlehorse.sdk.wfsdk.SpawnedThread;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.tests.TestFailure;
import io.littlehorse.tests.WorkflowLogicTest;
import java.util.Arrays;
import java.util.List;

public class ASChildThreadFails extends WorkflowLogicTest {

    public ASChildThreadFails(LHClient client, LHWorkerConfig workerConfig) {
        super(client, workerConfig);
    }

    public String getDescription() {
        return "Tests that WAIT_FOR_THREAD throws when child thread dies.";
    }

    private static final String FAILURE_OUTPUT = "this is the failure output";
    private static final String FAILURE_MESSAGE = "this is the failure message";

    public Workflow getWorkflowImpl() {
        return new WorkflowImpl(
            getWorkflowName(),
            thread -> {
                SpawnedThread child = thread.spawnThread(
                    subthread -> {
                        subthread.execute("as-obiwan");
                        subthread.fail(FAILURE_OUTPUT, "my-failure", FAILURE_MESSAGE);
                    },
                    "first-thread",
                    null
                );

                thread.execute("as-obiwan");
                thread.waitForThreads(child);
                thread.execute("as-obiwan");
            }
        );
    }

    public List<Object> getTaskWorkerObjects() {
        return Arrays.asList(new ASSimpleTask());
    }

    public List<String> launchAndCheckWorkflows(LHClient client)
        throws TestFailure, InterruptedException, LHApiError {
        String wfRunId = runWf(client);
        Thread.sleep(500);
        assertStatus(client, wfRunId, LHStatusPb.ERROR);

        // The parent should only execute one task.
        assertTaskOutputsMatch(client, wfRunId, 0, new ASSimpleTask().obiwan());

        NodeRunPb nr = getNodeRun(client, wfRunId, 0, 3);

        WaitForThreadsRunPb wtr = nr.getWaitThreads();
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
