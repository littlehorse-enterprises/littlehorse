package io.littlehorse.tests.cases.workflow;

import io.littlehorse.sdk.client.LHClient;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.config.LHWorkerConfig;
import io.littlehorse.sdk.common.exception.LHApiError;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.NodeRun;
import io.littlehorse.sdk.common.proto.PutExternalEventRequest;
import io.littlehorse.sdk.common.proto.ThreadRun;
import io.littlehorse.sdk.common.proto.VariableMutationType;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.sdk.common.proto.WaitForThreadsRun;
import io.littlehorse.sdk.common.proto.WfRun;
import io.littlehorse.sdk.wfsdk.SpawnedThread;
import io.littlehorse.sdk.wfsdk.ThreadBuilder;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.tests.TestFailure;
import io.littlehorse.tests.WorkflowLogicTest;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

// We don't yet add a test to see what happens when we try to wait for non-existent
// ThreadRun, because it's not really possible to fall into that trap given the
// safeguards of the java wf sdk. But we might do that test in the future.
public class BBWaitMultipleChildren extends WorkflowLogicTest {

    public BBWaitMultipleChildren(LHClient client, LHWorkerConfig workerConfig) {
        super(client, workerConfig);
    }

    public String getDescription() {
        return ("Tests happy path behavior of WAIT_FOR_THREADS node with "
                + "multiple threads to wait for.");
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
                    SpawnedThread child1 =
                            thread.spawnThread(
                                    this::thread1, "child-1", Map.of("input1", Map.of()));
                    SpawnedThread child2 =
                            thread.spawnThread(
                                    this::thread2, "child-2", Map.of("input2", Map.of()));

                    thread.waitForThreads(child1, child2);
                });
    }

    private void thread1(ThreadBuilder thread) {
        // We use this variable so that we can control whether the thread
        // fails by manipulating the external event content
        WfRunVariable eventOutput = thread.addVariable("input1", VariableType.JSON_OBJ);
        thread.mutate(
                eventOutput, VariableMutationType.ASSIGN, thread.waitForEvent("thread-1-event"));
        thread.execute("add-1", eventOutput.jsonPath("$.myInt"));
    }

    private void thread2(ThreadBuilder thread) {
        // We use this variable so that we can control whether the thread
        // fails by manipulating the external event content
        WfRunVariable eventOutput = thread.addVariable("input2", VariableType.JSON_OBJ);
        thread.mutate(
                eventOutput, VariableMutationType.ASSIGN, thread.waitForEvent("thread-2-event"));
        thread.execute("add-1", eventOutput.jsonPath("$.myInt"));
    }

    public List<Object> getTaskWorkerObjects() {
        return Arrays.asList(new BBSimpleTask());
    }

    public List<String> launchAndCheckWorkflows(LHClient client)
            throws TestFailure, InterruptedException, LHApiError {
        return Arrays.asList(
                happyPath(client), firstThreadFails(client), secondThreadFails(client));
    }

    private VariableValue generateEvent(String key, Object value) throws TestFailure {
        try {
            return LHLibUtil.objToVarVal(Map.of(key, value));
        } catch (LHSerdeError exn) {
            throw new TestFailure(this, "impossible: serde error: " + exn.getMessage());
        }
    }

    /*
     * This tests the happy path case where thread 1 finishes first.
     */
    private String happyPath(LHClient client) throws TestFailure, InterruptedException, LHApiError {
        String id = runWf(null, client);
        assertStatus(client, id, LHStatus.RUNNING);

        // Now make sure there are two child threads.
        WfRun wfRun = getWfRun(client, id);
        assertThat(
                wfRun.getThreadRunsCount() == 3, "WfRun " + id + " should have three threadruns");
        NodeRun waitForThreadNode = getNodeRun(client, id, 0, 3);
        assertThat(
                waitForThreadNode.getStatus() == LHStatus.RUNNING,
                "Wait for thread node should still be running! Wf: " + id);

        WaitForThreadsRun waitingThreads = waitForThreadNode.getWaitThreads();
        assertThat(
                waitingThreads.getThreadsCount() == 2,
                "Wait for thread node should have two threads it's waiting on! Wf: " + id);
        assertThat(
                waitingThreads.getThreads(0).getThreadRunNumber() == 1,
                "Expected first waiting thread to be thread 1! Wf: " + id);
        assertThat(
                waitingThreads.getThreads(1).getThreadRunNumber() == 2,
                "Expected second waiting thread to be thread 2! Wf: " + id);

        // Now we make the first thread finish
        ThreadRun thread1 = wfRun.getThreadRuns(1);
        assertThat(thread1.getStatus() == LHStatus.RUNNING, "Thread1 should be running! Wf: " + id);
        client.putExternalEvent(
                PutExternalEventRequest.newBuilder()
                        .setContent(generateEvent("myInt", 5))
                        .setWfRunId(id)
                        .setExternalEventDefName("thread-1-event")
                        .build());
        // There is a Task Node after the External Event Node, so we wait
        // for it to finish.
        Thread.sleep(200);
        wfRun = getWfRun(client, id);
        assertThat(
                wfRun.getStatus() == LHStatus.RUNNING, "WfRun should still be running! Wf: " + id);
        thread1 = wfRun.getThreadRuns(1);
        assertThat(thread1.getStatus() == LHStatus.COMPLETED, "Thread1 should be Done! Wf: " + id);
        ThreadRun entrypoint = wfRun.getThreadRuns(0);
        assertThat(
                entrypoint.getStatus() == LHStatus.RUNNING,
                "Entrypoint should be running becasue thread2 not done yet! Wf:" + id);
        waitingThreads = getNodeRun(client, id, 0, 3).getWaitThreads();
        assertThat(
                waitingThreads.getThreads(0).getThreadStatus() == LHStatus.COMPLETED,
                "Should have noticed that the first thread finished! WF: " + id);
        assertThat(
                waitingThreads.getThreads(1).getThreadStatus() == LHStatus.RUNNING,
                "Second thread should still be running! WF: " + id);

        // Now we complete the second thread.
        client.putExternalEvent(
                PutExternalEventRequest.newBuilder()
                        .setContent(generateEvent("myInt", 10))
                        .setWfRunId(id)
                        .setExternalEventDefName("thread-2-event")
                        .build());
        Thread.sleep(200);
        wfRun = getWfRun(client, id);
        assertThat(
                wfRun.getStatus() == LHStatus.COMPLETED,
                "WfRun should still be completed now! Wf: " + id);
        waitingThreads = getNodeRun(client, id, 0, 3).getWaitThreads();
        assertThat(
                waitingThreads.getThreads(1).getThreadStatus() == LHStatus.COMPLETED,
                "Should have noticed that the second thread finished! WF: " + id);
        return id;
    }

    private String firstThreadFails(LHClient client)
            throws TestFailure, InterruptedException, LHApiError {
        String id = runWf(null, client);
        assertStatus(client, id, LHStatus.RUNNING);

        // Now we make the first thread fail by sending a malformed event (recall
        // that the child thread expects $.myInt to be an int, not a string)
        client.putExternalEvent(
                PutExternalEventRequest.newBuilder()
                        .setContent(generateEvent("myInt", "some-string"))
                        .setWfRunId(id)
                        .setExternalEventDefName("thread-1-event")
                        .build());

        // There is a Task Node after the External Event Node, so we wait
        // for it to finish.
        Thread.sleep(200);
        WfRun wfRun = getWfRun(client, id);
        assertThat(
                wfRun.getStatus() == LHStatus.RUNNING, "WfRun should still be running! Wf: " + id);
        ThreadRun thread1 = wfRun.getThreadRuns(1);
        assertThat(thread1.getStatus() == LHStatus.ERROR, "Thread1 should have failed! Wf: " + id);

        NodeRun failedNode = getNodeRun(client, id, 1, 2);
        assertThat(
                failedNode.getStatus() == LHStatus.ERROR,
                "The task node of thread 1 should have failed with "
                        + "TASK_INPUT_VAR_SUB_ERROR, wf: "
                        + id);

        // This line here checks that the NodeRun failed due to input var sub error.
        assertThat(
                !failedNode.getTask().hasTaskRunId(),
                "The TaskRun should not have been created because the input vars"
                        + " were invalid! Wf: "
                        + id);
        assertThat(
                failedNode.getFailuresCount() == 1,
                "The failed node should have 1 failure. Wf: " + id);
        assertThat(
                failedNode.getFailures(0).getFailureName().equals("VAR_SUB_ERROR"),
                "The node should have failed with 'VAR_SUB_ERROR'! Wf: " + id);

        // Now we check that the ThreadRun was noticed as failed by the WaitThreadRun
        WaitForThreadsRun waitingThreads = getNodeRun(client, id, 0, 3).getWaitThreads();
        assertThat(
                waitingThreads.getThreads(0).getThreadStatus() == LHStatus.ERROR,
                "Should have noticed that the first thread Died! WF: " + id);
        assertThat(
                waitingThreads.getThreads(1).getThreadStatus() == LHStatus.RUNNING,
                "Second thread should still be running! WF: " + id);

        // Now we complete the second thread.
        client.putExternalEvent(
                PutExternalEventRequest.newBuilder()
                        .setContent(generateEvent("myInt", 10))
                        .setWfRunId(id)
                        .setExternalEventDefName("thread-2-event")
                        .build());
        Thread.sleep(200);
        wfRun = getWfRun(client, id);
        assertThat(
                wfRun.getStatus() == LHStatus.ERROR,
                "WfRun should have failed because first thread died! Wf: " + id);
        waitingThreads = getNodeRun(client, id, 0, 3).getWaitThreads();
        assertThat(
                waitingThreads.getThreads(1).getThreadStatus() == LHStatus.COMPLETED,
                "Should have noticed that the second thread finished! WF: " + id);
        return id;
    }

    private String secondThreadFails(LHClient client)
            throws TestFailure, InterruptedException, LHApiError {
        String id = runWf(null, client);
        assertStatus(client, id, LHStatus.RUNNING);

        // Now we make the first thread complete
        client.putExternalEvent(
                PutExternalEventRequest.newBuilder()
                        .setContent(generateEvent("myInt", 137))
                        .setWfRunId(id)
                        .setExternalEventDefName("thread-1-event")
                        .build());

        // There is a Task Node after the External Event Node, so we wait
        // for it to finish.
        Thread.sleep(200);
        WfRun wfRun = getWfRun(client, id);
        assertThat(
                wfRun.getStatus() == LHStatus.RUNNING, "WfRun should still be running! Wf: " + id);
        ThreadRun thread1 = wfRun.getThreadRuns(1);
        assertThat(
                thread1.getStatus() == LHStatus.COMPLETED,
                "Thread1 should have finished! Wf: " + id);

        // Now we check that the ThreadRun was noticed as failed by the WaitThreadRun
        WaitForThreadsRun waitingThreads = getNodeRun(client, id, 0, 3).getWaitThreads();
        assertThat(
                waitingThreads.getThreads(0).getThreadStatus() == LHStatus.COMPLETED,
                "Should have noticed that the first thread Died! WF: " + id);
        assertThat(
                waitingThreads.getThreads(1).getThreadStatus() == LHStatus.RUNNING,
                "Second thread should still be running! WF: " + id);

        // Now we fail the second thread.
        client.putExternalEvent(
                PutExternalEventRequest.newBuilder()
                        .setContent(generateEvent("myInt", "not-an-integer"))
                        .setWfRunId(id)
                        .setExternalEventDefName("thread-2-event")
                        .build());
        Thread.sleep(200);
        wfRun = getWfRun(client, id);
        assertThat(
                wfRun.getStatus() == LHStatus.ERROR,
                "WfRun should have failed because second thread died! Wf: " + id);
        waitingThreads = getNodeRun(client, id, 0, 3).getWaitThreads();
        assertThat(
                waitingThreads.getThreads(1).getThreadStatus() == LHStatus.ERROR,
                "Should have noticed that the second thread failed! WF: " + id);
        return id;
    }
}

class BBSimpleTask {

    @LHTaskMethod("add-1")
    public int addOne(int input) {
        return input + 1;
    }
}
