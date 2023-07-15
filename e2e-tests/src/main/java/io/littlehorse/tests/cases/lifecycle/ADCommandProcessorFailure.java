package io.littlehorse.tests.cases.lifecycle;

import io.littlehorse.sdk.client.LHClient;
import io.littlehorse.sdk.common.config.LHWorkerConfig;
import io.littlehorse.sdk.common.exception.LHApiError;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.sdk.worker.LHTaskWorker;
import io.littlehorse.tests.Test;

/*
 * This test involves deploying a WfSpec, then deleting a TaskDef, then
 * running the WfRun. Before the test was added, this would cause the
 * entire LH Server to crash, restart, then crash, all over again. Because
 * the CommandProcessor#process() method call would fail, then the same event
 * in the Core Command topic was re-processed without committing the consumer
 * offsets. So this does three things:
 *
 * 1. Re-create the behavior where the LH Server crashes
 * 2. Add a safeguard to CommandProcessor:process() which prevents the crash
 * 3. Verify the crash is fixed
 * 4. Make WfRun fail gracefully if TaskDef is deleted.
 */
public class ADCommandProcessorFailure extends Test {

    public static final String TASK_DEF_NAME = "ad-lifecycle-taskdef";
    public static final String WF_SPEC_NAME = "ad-lifecycle-wfspec";
    private String wfRunId;

    public ADCommandProcessorFailure(LHClient client, LHWorkerConfig config) {
        super(client, config);
    }

    public String getDescription() {
        return """
Tests that when a malformed command is sent to the LH Server, the request is
aborted and returned back to the client. In the past, the AsyncWaiter would never
get the `onResponseReceived` callback, so the client would hang for 60 seconds
until the periodic cronjob that cleans up old request waiters, and then it would
return 'RECORDED_NOT_PROCESSED'.
                """;
    }

    public void test() throws LHApiError, InterruptedException {
        LHTaskWorker worker = new LHTaskWorker(
            new TaskWfSpecLifecycleWorker(),
            TASK_DEF_NAME,
            workerConfig
        );
        worker.registerTaskDef(true);

        new WorkflowImpl(
            WF_SPEC_NAME,
            thread -> {
                thread.execute(TASK_DEF_NAME);
            }
        )
            .registerWfSpec(client);

        Thread.sleep(100); // Wait for the data to propagate

        // Delete the TaskDef
        client.deleteTaskDef(TASK_DEF_NAME);

        Thread.sleep(120);

        long start = System.currentTimeMillis();
        LHApiError caught = null;
        try {
            wfRunId = client.runWf(WF_SPEC_NAME, null, null);
        } catch (LHApiError exn) {
            caught = exn;
        }
        long end = System.currentTimeMillis();

        if (end - start > 200) {
            throw new RuntimeException(
                "It appears the API hung when it should have thrown an error!"
            );
        }
        if (caught == null) {
            throw new RuntimeException("The API didn't throw an error back!");
        }
    }

    public void cleanup() throws LHApiError {
        try {
            client.deleteTaskDef(TASK_DEF_NAME);
            client.deleteWfSpec(WF_SPEC_NAME, 0);
            client.deleteWfRun(wfRunId);
        } catch (Exception exn) {}
    }
}

class TaskWfSpecLifecycleWorker {

    @LHTaskMethod(ADCommandProcessorFailure.TASK_DEF_NAME)
    public String foo() {
        return "hi";
    }
}
