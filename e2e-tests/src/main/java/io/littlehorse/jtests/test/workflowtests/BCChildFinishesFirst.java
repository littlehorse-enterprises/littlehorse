package io.littlehorse.jtests.test.workflowtests;

import io.littlehorse.jtests.test.LogicTestFailure;
import io.littlehorse.jtests.test.WorkflowLogicTest;
import io.littlehorse.sdk.client.LHClient;
import io.littlehorse.sdk.common.config.LHWorkerConfig;
import io.littlehorse.sdk.common.exception.LHApiError;
import io.littlehorse.sdk.common.proto.LHStatusPb;
import io.littlehorse.sdk.wfsdk.SpawnedThread;
import io.littlehorse.sdk.wfsdk.ThreadBuilder;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskMethod;
import java.util.Arrays;
import java.util.List;

// We don't yet add a test to see what happens when we try to wait for non-existent
// ThreadRun, because it's not really possible to fall into that trap given the
// safeguards of the java wf sdk. But we might do that test in the future.
public class BCChildFinishesFirst extends WorkflowLogicTest {

    public BCChildFinishesFirst(LHClient client, LHWorkerConfig workerConfig) {
        super(client, workerConfig);
    }

    public String getDescription() {
        return (
            "Tests happy path behavior of WAIT_FOR_THREADS node with " +
            "multiple threads to wait for."
        );
    }

    /*
     * This workflow basically spawns two child threads and waits for them.
     * Each child thread listens for an external event, then executes a
     * task after that event has come through.
     *
     * The Child workflows implicitly assume that the external event is a
     * JSON_OBJ with structhre `{"myInt": <some integer>}`, so we can use
     * that to make the child workflows fail in order to test certain edge
     * cases.
     *
     * Additionally, we can use the external event to control the order in
     * which the child threads complete, to verify that when the children
     * are in progress that the status is properly reflected in the workflow.
     */
    public Workflow getWorkflowImpl() {
        return new WorkflowImpl(
            getWorkflowName(),
            thread -> {
                SpawnedThread child = thread.spawnThread(this::child, "child", null);
                thread.sleepSeconds(1);
                thread.waitForThreads(child);
            }
        );
    }

    private void child(ThreadBuilder thread) {
        thread.execute("bc-obiwan");
    }

    public List<Object> getTaskWorkerObjects() {
        return Arrays.asList(new BCSimpleTask());
    }

    public List<String> launchAndCheckWorkflows(LHClient client)
        throws LogicTestFailure, InterruptedException, LHApiError {
        String wfRunId = runWf(client);
        Thread.sleep(100);
        assertStatus(client, wfRunId, LHStatusPb.RUNNING);
        assertThreadStatus(client, wfRunId, 1, LHStatusPb.COMPLETED);

        Thread.sleep(5000); // wait for entrypoint
        assertStatus(client, wfRunId, LHStatusPb.COMPLETED);

        return Arrays.asList(wfRunId);
    }
}

class BCSimpleTask {

    @LHTaskMethod("bc-obiwan")
    public String obiWan() {
        return "hello there";
    }
}
