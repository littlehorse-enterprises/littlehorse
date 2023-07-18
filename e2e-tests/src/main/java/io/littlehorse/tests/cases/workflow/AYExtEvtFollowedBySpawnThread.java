package io.littlehorse.tests.cases.workflow;

import io.littlehorse.sdk.client.LHClient;
import io.littlehorse.sdk.common.config.LHWorkerConfig;
import io.littlehorse.sdk.common.exception.LHApiError;
import io.littlehorse.sdk.common.proto.LHStatusPb;
import io.littlehorse.sdk.wfsdk.SpawnedThread;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.tests.TestFailure;
import io.littlehorse.tests.WorkflowLogicTest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AYExtEvtFollowedBySpawnThread extends WorkflowLogicTest {

    public AYExtEvtFollowedBySpawnThread(
        LHClient client,
        LHWorkerConfig workerConfig
    ) {
        super(client, workerConfig);
    }

    public String getDescription() {
        return (
            "Tests that we can neatly segway from EXTERNAL_EVENT to " +
            "SPAWN_THREAD node."
        );
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
                thread.waitForThreads(child);
            }
        );
    }

    public List<Object> getTaskWorkerObjects() {
        return Arrays.asList(new AYSimpleTask());
    }

    public List<String> launchAndCheckWorkflows(LHClient client)
        throws TestFailure, InterruptedException, LHApiError {
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
