package e2e;

import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.ResumeWfRunRequest;
import io.littlehorse.sdk.common.proto.StopWfRunRequest;
import io.littlehorse.sdk.common.proto.TaskStatus;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.LHWorkflow;
import io.littlehorse.test.WithWorkers;
import io.littlehorse.test.WorkflowVerifier;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

@LHTest(externalEventNames = {StopWfRunTest.EVENT_NAME, StopWfRunTest.INTERRUPT})
public class StopWfRunTest {

    static final String EVENT_NAME = "stop-wf-run-continue";
    static final String INTERRUPT = "stop-wf-run-interrupt";
    private static final String TASK_NAME = "stop-wf-run-task";
    private final TaskWorker taskWorker = new TaskWorker();

    private LittleHorseBlockingStub client;
    private WorkflowVerifier verifier;

    @LHWorkflow("stop-wf-run")
    public Workflow workflow() {
        return Workflow.newWorkflow(
                "stop-wf-run",
                thread -> {
                    thread.execute(TASK_NAME).timeout(60);
                    thread.waitForEvent(EVENT_NAME);
                },
                new LHConfig());
    }

    @LHWorkflow("stop-interrupt-handler")
    public Workflow interruptWorkflow() {
        return Workflow.newWorkflow(
                "stop-interrupt-handler",
                thread -> {
                    thread.registerInterruptHandler(
                            INTERRUPT, handler -> handler.execute(TASK_NAME).timeout(60));
                    thread.waitForEvent(EVENT_NAME);
                },
                new LHConfig());
    }

    @Test
    @WithWorkers("worker")
    void shouldWaitForInFlightInterruptTaskBeforeHalting() {
        try {
            verifier.prepareRun(interruptWorkflow())
                    .thenSendExternalEventWithContent(INTERRUPT, null)
                    .waitForTaskStatus(1, 1, TaskStatus.TASK_RUNNING)
                    .waitForThreadRunStatus(0, LHStatus.HALTED)
                    .thenVerifyWfRun(run -> client.stopWfRun(StopWfRunRequest.newBuilder()
                            .setWfRunId(run.getId())
                            .build()))
                    .waitForThreadRunStatus(1, LHStatus.HALTING)
                    .waitForStatus(LHStatus.RUNNING)
                    .thenVerifyWfRun(run -> taskWorker.finish.countDown())
                    .waitForTaskStatus(1, 1, TaskStatus.TASK_SUCCESS)
                    .waitForThreadRunStatus(1, LHStatus.HALTED)
                    .waitForStatus(LHStatus.HALTED)
                    .start();
        } finally {
            taskWorker.finish.countDown();
        }
    }

    public TaskWorker worker() {
        return taskWorker;
    }

    @Test
    @WithWorkers("worker")
    void shouldHaltWhenInFlightTaskFinishesAndResumeFromTask() {
        try {
            verifier.prepareRun(workflow())
                    .waitForTaskStatus(0, 1, TaskStatus.TASK_RUNNING)
                    .thenVerifyWfRun(run -> client.stopWfRun(StopWfRunRequest.newBuilder()
                            .setWfRunId(run.getId())
                            .build()))
                    .waitForThreadRunStatus(0, LHStatus.HALTING)
                    .waitForNodeRunStatus(0, 1, LHStatus.HALTING)
                    .thenVerifyWfRun(run -> taskWorker.finish.countDown())
                    .waitForTaskStatus(0, 1, TaskStatus.TASK_SUCCESS)
                    .waitForNodeRunStatus(0, 1, LHStatus.HALTED)
                    .waitForThreadRunStatus(0, LHStatus.HALTED)
                    .waitForStatus(LHStatus.HALTED)
                    .thenVerifyWfRun(run -> client.resumeWfRun(ResumeWfRunRequest.newBuilder()
                            .setWfRunId(run.getId())
                            .build()))
                    .waitForStatus(LHStatus.RUNNING)
                    .waitForThreadRunStatus(0, LHStatus.RUNNING)
                    .waitForNodeRunStatus(0, 1, LHStatus.COMPLETED)
                    .waitForNodeRunStatus(0, 2, LHStatus.RUNNING)
                    .thenSendExternalEventWithContent(EVENT_NAME, null)
                    .waitForStatus(LHStatus.COMPLETED)
                    .start();
        } finally {
            taskWorker.finish.countDown();
        }
    }

    @Test
    @WithWorkers("worker")
    void shouldHaltAndResumeImmediatelyAtExternalEvent() {
        taskWorker.finish.countDown();
        verifier.prepareRun(workflow())
                .waitForNodeRunStatus(0, 2, LHStatus.RUNNING)
                .thenVerifyWfRun(run -> client.stopWfRun(
                        StopWfRunRequest.newBuilder().setWfRunId(run.getId()).build()))
                .waitForNodeRunStatus(0, 2, LHStatus.HALTED)
                .waitForThreadRunStatus(0, LHStatus.HALTED)
                .waitForStatus(LHStatus.HALTED)
                .thenVerifyWfRun(run -> client.resumeWfRun(
                        ResumeWfRunRequest.newBuilder().setWfRunId(run.getId()).build()))
                .waitForNodeRunStatus(0, 2, LHStatus.RUNNING)
                .waitForThreadRunStatus(0, LHStatus.RUNNING)
                .waitForStatus(LHStatus.RUNNING)
                .thenSendExternalEventWithContent(EVENT_NAME, null)
                .waitForStatus(LHStatus.COMPLETED)
                .start();
    }

    public static class TaskWorker {
        private final CountDownLatch finish = new CountDownLatch(1);

        @LHTaskMethod(TASK_NAME)
        public String execute() throws InterruptedException {
            if (!finish.await(30, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Test did not release the task");
            }
            return "done";
        }
    }
}
