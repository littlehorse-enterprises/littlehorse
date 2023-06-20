package io.littlehorse.jtests.test.workflowtests;

import io.littlehorse.jlib.client.LHClient;
import io.littlehorse.jlib.common.config.LHWorkerConfig;
import io.littlehorse.jlib.common.exception.LHApiError;
import io.littlehorse.jlib.common.proto.LHStatusPb;
import io.littlehorse.jlib.common.proto.NodeRunPb;
import io.littlehorse.jlib.common.proto.WaitThreadRunPb;
import io.littlehorse.jlib.wfsdk.SpawnedThread;
import io.littlehorse.jlib.wfsdk.Workflow;
import io.littlehorse.jlib.wfsdk.internal.WorkflowImpl;
import io.littlehorse.jlib.worker.LHTaskMethod;
import io.littlehorse.jtests.test.LogicTestFailure;
import io.littlehorse.jtests.test.WorkflowLogicTest;
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
                thread.waitForThread(child);
                thread.execute("as-obiwan");
            }
        );
    }

    public List<Object> getTaskWorkerObjects() {
        return Arrays.asList(new ASSimpleTask());
    }

    public List<String> launchAndCheckWorkflows(LHClient client)
        throws LogicTestFailure, InterruptedException, LHApiError {
        String wfRunId = runWf(client);
        Thread.sleep(500);
        assertStatus(client, wfRunId, LHStatusPb.ERROR);

        // The parent should only execute one task.
        assertTaskOutputsMatch(client, wfRunId, 0, new ASSimpleTask().obiwan());

        NodeRunPb nr = getNodeRun(client, wfRunId, 0, 3);
        WaitThreadRunPb wtr = nr.getWaitThread();
        if (wtr.getThreadRunNumber() != 1) {
            throw new LogicTestFailure(
                this,
                wfRunId + " should have waited for thread 1"
            );
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
