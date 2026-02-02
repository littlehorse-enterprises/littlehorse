package e2e;

import static io.littlehorse.sdk.common.proto.LHStatus.*;
import static org.assertj.core.api.Assertions.*;

import io.littlehorse.sdk.wfsdk.SpawnedThread;
import io.littlehorse.sdk.wfsdk.SpawnedThreads;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.LHWorkflow;
import io.littlehorse.test.WorkflowVerifier;
import java.util.Map;
import org.junit.jupiter.api.Test;

@LHTest(externalEventNames = {"wait-for-first-event", "wait-for-second-event", "wait-for-fail-event"})
public class WaitForOneOfTest {

    private WorkflowVerifier verifier;

    @LHWorkflow("wait-for-first-of-test")
    private Workflow waitForFirstOfTest;

    @LHWorkflow("wait-for-any-of-test")
    private Workflow waitForAnyOfTest;

    @Test
    void waitForFirstOfShouldHaltOtherChildWhenFirstSucceeds() {
        verifier.prepareRun(waitForFirstOfTest)
                .thenSendExternalEventWithContent("wait-for-first-event", "asdf")
                .waitForStatus(COMPLETED)
                .waitForThreadRunStatus(2, HALTED)
                .waitForThreadRunStatus(3, HALTED)
                .waitForThreadRunStatus(1, COMPLETED)
                .start();
    }

    @Test
    void waitForFirstOfShouldFailIfFirstThreadFails() {
        verifier.prepareRun(waitForFirstOfTest)
                .thenSendExternalEventWithContent("wait-for-fail-event", "asdf")
                .waitForStatus(EXCEPTION)
                .waitForThreadRunStatus(2, HALTED)
                .waitForThreadRunStatus(1, HALTED)
                .waitForThreadRunStatus(3, EXCEPTION)
                .start();
    }

    @Test
    void waitForAnyOfShouldHaltOtherChildWhenFirstSucceeds() {
        verifier.prepareRun(waitForAnyOfTest)
                .thenSendExternalEventWithContent("wait-for-first-event", "asdf")
                .waitForStatus(COMPLETED)
                .waitForThreadRunStatus(2, HALTED)
                .waitForThreadRunStatus(3, HALTED)
                .waitForThreadRunStatus(1, COMPLETED)
                .start();
    }

    @Test
    void waitForAnyOfShouldIgnoreFailedFirstThread() {
        verifier.prepareRun(waitForAnyOfTest)
                .thenSendExternalEventWithContent("wait-for-fail-event", "asdf")
                .thenSendExternalEventWithContent("wait-for-second-event", "asdf")
                .waitForThreadRunStatus(3, EXCEPTION)
                .waitForStatus(COMPLETED)
                .waitForThreadRunStatus(2, COMPLETED)
                .waitForThreadRunStatus(1, HALTED)
                .start();
    }

    @Test
    void waitForAnyOfShouldThrowERRORWhenAllFail() {
        verifier.prepareRun(waitForAnyOfTest)
                .thenSendExternalEventWithContent("wait-for-fail-event", "asdf")
                .waitForThreadRunStatus(3, EXCEPTION)
                .waitForThreadRunStatus(2, ERROR)
                .waitForThreadRunStatus(1, ERROR)
                .waitForStatus(ERROR)
                .start();
    }

    @LHWorkflow("wait-for-any-of-test")
    public Workflow buildWaitForAnyOfSuccessWorkflow() {
        return new WorkflowImpl("wait-for-any-of-test", wf -> {
            SpawnedThread child1 = wf.spawnThread(
                    child -> {
                        child.waitForEvent("wait-for-first-event").timeout(2);
                    },
                    "child-1",
                    Map.of());

            SpawnedThread child2 = wf.spawnThread(
                    child -> {
                        child.waitForEvent("wait-for-second-event").timeout(2);
                    },
                    "child-2",
                    Map.of());

            SpawnedThread child3 = wf.spawnThread(
                    child -> {
                        child.waitForEvent("wait-for-fail-event");
                        child.fail("business-exception", "some message");
                    },
                    "child-3",
                    Map.of());

            wf.waitForAnyOf(SpawnedThreads.of(child1, child2, child3));
        });
    }

    @LHWorkflow("wait-for-first-of-test")
    public Workflow buildWaitForAnyOfIgnoreFailureWorkflow() {
        return new WorkflowImpl("wait-for-first-of-test", wf -> {
            SpawnedThread child1 = wf.spawnThread(
                    child -> {
                        child.waitForEvent("wait-for-first-event").timeout(2);
                    },
                    "child-1",
                    Map.of());

            SpawnedThread child2 = wf.spawnThread(
                    child -> {
                        child.waitForEvent("wait-for-second-event").timeout(2);
                    },
                    "child-2",
                    Map.of());

            SpawnedThread child3 = wf.spawnThread(
                    child -> {
                        child.waitForEvent("wait-for-fail-event");
                        child.fail("business-exception", "some message");
                    },
                    "child-3",
                    Map.of());

            wf.waitForFirstOf(SpawnedThreads.of(child1, child2, child3));
        });
    }
}
