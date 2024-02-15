package e2e;

import java.util.Map;

import org.junit.jupiter.api.Test;

import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.wfsdk.SpawnedThreads;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.LHWorkflow;
import io.littlehorse.test.WorkflowVerifier;

/*
 * Tests that Interrupts cause NodeRun lifecycle to move properly.
 */
@LHTest(externalEventNames = {
    InterruptLifecycleTest.PARENT_EVENT,
    InterruptLifecycleTest.INTERRUPT_TRIGGER,
    InterruptLifecycleTest.COMPLETE_INTERRUPT_HANDLER,
    InterruptLifecycleTest.CHILD_EVENT
})
public class InterruptLifecycleTest {
    public static final String PARENT_EVENT = "ilt-parent-event";
    public static final String CHILD_EVENT = "ilt-child-event";
    public static final String INTERRUPT_TRIGGER = "ilt-interrupt-trigger";
    public static final String COMPLETE_INTERRUPT_HANDLER = "ilt-complete-interrupt-handler";

    @LHWorkflow("interrupt-lifecycle-test")
    public Workflow interruptLifecycleTest;

    private WorkflowVerifier verifier;

    @Test
    void shouldCompleteWithNoInterrupts() {
        verifier.prepareRun(interruptLifecycleTest)
            .thenSendExternalEventJsonContent(PARENT_EVENT, null)
            .thenSendExternalEventJsonContent(PARENT_EVENT, null)
            .waitForStatus(LHStatus.COMPLETED)
            .start();
    }

    @LHWorkflow("interrupt-lifecycle-test")
    public Workflow getInterruptLifecycleTestWf() {
        return Workflow.newWorkflow("interrupt-lifecycle-test", wf -> {
            wf.registerInterruptHandler(INTERRUPT_TRIGGER, handler -> {
                handler.waitForEvent(COMPLETE_INTERRUPT_HANDLER);
            });

            wf.waitForEvent(PARENT_EVENT);
            wf.sleepSeconds(1);
            wf.execute("dummy-task");

            // Spawn and wait for child
            wf.waitForThreads(SpawnedThreads.of(wf.spawnThread(child -> {
                child.waitForEvent(CHILD_EVENT);
            }, "child", Map.of())));
        });
    }

    @LHTaskMethod("dummy-task")
    public String obiwan() {
        return "hello there";
    }
}
