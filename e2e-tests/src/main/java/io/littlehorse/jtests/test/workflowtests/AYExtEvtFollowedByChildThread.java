package io.littlehorse.jtests.test.workflowtests;

import io.littlehorse.jlib.client.LHClient;
import io.littlehorse.jlib.common.config.LHWorkerConfig;
import io.littlehorse.jlib.common.exception.LHApiError;
import io.littlehorse.jlib.common.proto.LHStatusPb;
import io.littlehorse.jlib.wfsdk.SpawnedThread;
import io.littlehorse.jlib.wfsdk.Workflow;
import io.littlehorse.jlib.wfsdk.internal.WorkflowImpl;
import io.littlehorse.jlib.worker.LHTaskMethod;
import io.littlehorse.jtests.test.LogicTestFailure;
import io.littlehorse.jtests.test.WorkflowLogicTest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AYExtEvtFollowedByChildThread extends WorkflowLogicTest {

    public AYExtEvtFollowedByChildThread(
        LHClient client,
        LHWorkerConfig workerConfig
    ) {
        super(client, workerConfig);
    }

    public String getDescription() {
        return "Tests that WAIT_FOR_THREAD throws when child thread dies.";
    }

    public Workflow getWorkflowImpl() {
        return new WorkflowImpl(
            getWorkflowName(),
            thread -> {
                thread.waitForEvent("ay-some-event");
                SpawnedThread child = thread.spawnThread(
                    subthread -> {
                        subthread.execute("ay-task");
                    },
                    "first-thread",
                    null
                );

                thread.execute("ay-task");
                thread.waitForThread(child);
            }
        );
    }

    public List<Object> getTaskWorkerObjects() {
        return Arrays.asList(new AYSimpleTask());
    }

    public List<String> launchAndCheckWorkflows(LHClient client)
        throws LogicTestFailure, InterruptedException, LHApiError {
        List<String> out = new ArrayList<>();

        String wfRunId = runWf(client);
        Thread.sleep(200);
        sendEvent(client, wfRunId, "ay-some-event", "dummy content", null);
        Thread.sleep(600);
        assertStatus(client, wfRunId, LHStatusPb.COMPLETED);

        // both threads should execute the obiwan() task once
        assertTaskOutputsMatch(client, wfRunId, 0, "hello there");
        assertTaskOutputsMatch(client, wfRunId, 1, "hello there");

        out.add(wfRunId);
        return out;
    }
}

class AYSimpleTask {

    @LHTaskMethod("ay-task")
    public String obiwan() {
        return "hello there";
    }
}
