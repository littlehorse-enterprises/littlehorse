package e2e;

import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.LHWorkflow;
import io.littlehorse.test.WorkflowVerifier;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/*
 * Tests edge cases in the halting/unhalting logic of threadruns and noderuns.
 */
@LHTest(
        externalEventNames = {
            InterruptOnExternalEventTest.PARENT_EVENT,
            InterruptOnExternalEventTest.INTERRUPT_TRIGGER,
            InterruptOnExternalEventTest.CHILD_EVENT
        })
public class InterruptOnExternalEventTest {

    public static final String PARENT_EVENT = "iaeet-parent";
    public static final String INTERRUPT_TRIGGER = "iaeet-interupt-trigger";
    public static final String CHILD_EVENT = "iaeet-child";

    private WorkflowVerifier verifier;

    @LHWorkflow("interrupt-on-external-event")
    private Workflow interruptOnExternalEvent;

    @Test
    void testHappyPath() {
        verifier.prepareRun(interruptOnExternalEvent)
                .thenSendExternalEventJsonContent(PARENT_EVENT, null)
                .thenVerifyWfRun(wfRun -> {
                    Assertions.assertThat(wfRun.getStatus()).isEqualTo(LHStatus.COMPLETED);
                    Assertions.assertThat(wfRun.getThreadRunsCount()).isEqualTo(1);
                })
                .start();
    }

    @Test
    void parentShouldHaltDuringInterrupt() {
        verifier.prepareRun(interruptOnExternalEvent)
                .thenSendExternalEventJsonContent(INTERRUPT_TRIGGER, null)
                .thenVerifyWfRun(wfRun -> {
                    Assertions.assertThat(wfRun.getStatus()).isEqualTo(LHStatus.RUNNING);
                    Assertions.assertThat(wfRun.getThreadRunsCount()).isEqualTo(2);
                    Assertions.assertThat(wfRun.getThreadRuns(0).getStatus()).isEqualTo(LHStatus.HALTED);
                    Assertions.assertThat(wfRun.getThreadRuns(1).getStatus()).isEqualTo(LHStatus.RUNNING);
                })
                .thenVerifyNodeRun(0, 1, nodeRun -> {
                    Assertions.assertThat(nodeRun.getStatus()).isEqualTo(LHStatus.HALTED);
                })
                .thenSendExternalEventJsonContent(CHILD_EVENT, null)
                .thenVerifyWfRun(wfRun -> {
                    Assertions.assertThat(wfRun.getThreadRuns(1).getStatus()).isEqualTo(LHStatus.COMPLETED);
                })
                .thenVerifyNodeRun(0, 1, nodeRun -> {
                    Assertions.assertThat(nodeRun.getStatus()).isEqualTo(LHStatus.RUNNING);
                })
                .thenSendExternalEventJsonContent(PARENT_EVENT, null)
                .thenVerifyWfRun(wfRun -> {
                    Assertions.assertThat(wfRun.getStatus()).isEqualTo(LHStatus.COMPLETED);
                })
                .start();
    }

    @Test
    void parentShouldContinueAfterInterruptFinishesWhenEventArrivesWhileHalted() {
        verifier.prepareRun(interruptOnExternalEvent)
                .thenSendExternalEventJsonContent(INTERRUPT_TRIGGER, null)
                .thenVerifyWfRun(wfRun -> {
                    Assertions.assertThat(wfRun.getStatus()).isEqualTo(LHStatus.RUNNING);
                    Assertions.assertThat(wfRun.getThreadRunsCount()).isEqualTo(2);
                    Assertions.assertThat(wfRun.getThreadRuns(0).getStatus()).isEqualTo(LHStatus.HALTED);
                    Assertions.assertThat(wfRun.getThreadRuns(1).getStatus()).isEqualTo(LHStatus.RUNNING);
                })
                .thenVerifyNodeRun(0, 1, nodeRun -> {
                    Assertions.assertThat(nodeRun.getStatus()).isEqualTo(LHStatus.HALTED);
                })
                .thenSendExternalEventJsonContent(PARENT_EVENT, null)
                .thenVerifyNodeRun(0, 1, nodeRun -> {
                    Assertions.assertThat(nodeRun.getStatus()).isEqualTo(LHStatus.HALTED);
                })
                .thenVerifyWfRun(wfRun -> {
                    Assertions.assertThat(wfRun.getStatus()).isEqualTo(LHStatus.RUNNING);
                    Assertions.assertThat(wfRun.getThreadRuns(0).getStatus()).isEqualTo(LHStatus.HALTED);
                })
                .thenSendExternalEventJsonContent(CHILD_EVENT, null)
                .thenVerifyWfRun(wfRun -> {
                    Assertions.assertThat(wfRun.getStatus()).isEqualTo(LHStatus.COMPLETED);
                })
                .thenVerifyNodeRun(0, 1, nodeRun -> {
                    Assertions.assertThat(nodeRun.getStatus()).isEqualTo(LHStatus.COMPLETED);
                })
                .start();
    }

    /*
     * Main Thread: just waits for an external event. There is an interrupt trigger.
     *
     * Interrupt Thread: waits for a different external event.
     *
     * This allows us to test edge cases by "waking up" the interrupt and the main
     * thread at different times. Because we control the advancing of the threads (by
     * posting external events), we can inspect noderuns in detail at specific times.
     */
    @LHWorkflow("interrupt-on-external-event")
    public Workflow getInterruptOnExternalEventWf() {
        return Workflow.newWorkflow("interrupt-on-external-event", parent -> {
            parent.waitForEvent(PARENT_EVENT);

            parent.registerInterruptHandler(INTERRUPT_TRIGGER, child -> {
                child.waitForEvent(CHILD_EVENT);
            });
        });
    }
}
