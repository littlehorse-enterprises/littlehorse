package e2e;


import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import io.littlehorse.sdk.common.proto.Interrupted;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.ThreadHaltReason;
import io.littlehorse.sdk.common.proto.ThreadRun;
import io.littlehorse.sdk.common.proto.ThreadType;
import io.littlehorse.sdk.common.proto.ThreadHaltReason.ReasonCase;
import io.littlehorse.sdk.wfsdk.SpawnedThread;
import io.littlehorse.sdk.wfsdk.SpawnedThreads;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.LHWorkflow;
import io.littlehorse.test.WorkflowVerifier;

/*
 * Verifies edge cases when interrupting parents and children at the same time.
 */
@LHTest(externalEventNames = {
    InterruptOnChildThreadTest.PARENT_INTERRUPT_TRIGGER,
    InterruptOnChildThreadTest.COMPLETE_PARENT,
    InterruptOnChildThreadTest.CHILD_INTERRUPT_TRIGGER,
    InterruptOnChildThreadTest.COMPLETE_CHILD
})
public class InterruptOnChildThreadTest {

    public static final String PARENT_INTERRUPT_TRIGGER = "iact-parent-trigger";
    public static final String COMPLETE_PARENT = "iact-complete-parent";
    public static final String CHILD_INTERRUPT_TRIGGER = "iact-child-trigger";
    public static final String COMPLETE_CHILD = "iact-complete-child";

    @LHWorkflow("interrupt-and-child-thread")
    public Workflow interruptAndChildThreadWf;

    private WorkflowVerifier verifier;

    @Test
    void shouldCompleteWithNoInterrupts() {
        verifier.prepareRun(interruptAndChildThreadWf)
                .waitForThreadRunStatus(1, LHStatus.RUNNING)
                .thenSendExternalEventJsonContent(COMPLETE_CHILD, null)
                .thenVerifyWfRun(wfRun -> {
                    ThreadRun child = wfRun.getThreadRuns(1);
                    Assertions.assertThat(child.getStatus()).isEqualTo(LHStatus.COMPLETED);
                    Assertions.assertThat(child.getParentThreadId()).isEqualTo(0);
                    
                    ThreadRun parent = wfRun.getThreadRuns(0);
                    Assertions.assertThat(parent.getCurrentNodePosition()).isEqualTo(3);
                })
                .thenVerifyNodeRun(0, 3, nodeRun -> {
                    Assertions.assertThat(nodeRun.getStatus()).isEqualTo(LHStatus.RUNNING);
                })
                .thenSendExternalEventJsonContent(COMPLETE_PARENT, null)
                .thenVerifyWfRun(wfRun -> {
                    Assertions.assertThat(wfRun.getStatus()).isEqualTo(LHStatus.COMPLETED);
                })
                .start();
    }

    @Test
    void shouldCompleteWhenChildInterrupted() {
        verifier.prepareRun(interruptAndChildThreadWf)
                .waitForThreadRunStatus(1, LHStatus.RUNNING)
                .thenSendExternalEventJsonContent(CHILD_INTERRUPT_TRIGGER, null)
                .thenVerifyWfRun(wfRun -> {
                    // Ensure that the WfRun is properly interrupted
                    ThreadRun child = wfRun.getThreadRuns(1);
                    Assertions.assertThat(child.getStatus()).isEqualTo(LHStatus.HALTED);
                    Assertions.assertThat(child.getHaltReasonsCount()).isEqualTo(1);

                    ThreadHaltReason haltReason = child.getHaltReasons(0);
                    Assertions.assertThat(haltReason.getReasonCase()).isEqualTo(ReasonCase.INTERRUPTED);
                    Interrupted subHaltReason = haltReason.getInterrupted();
                    Assertions.assertThat(subHaltReason.getInterruptThreadId()).isEqualTo(2);

                    ThreadRun interruptHandler = wfRun.getThreadRuns(2);
                    Assertions.assertThat(interruptHandler.getType()).isEqualTo(ThreadType.INTERRUPT);
                })
                .thenVerifyNodeRun(1, 1, extEvtNodeRun -> {
                    // The "Wait for event" nodeRun in the *CHILD* should be HALTED since
                    // we interrupted the child
                    Assertions.assertThat(extEvtNodeRun.getStatus()).isEqualTo(LHStatus.HALTED);
                })
                // Wait for interrupt to complete
                .waitForThreadRunStatus(2, LHStatus.COMPLETED)
                .thenSendExternalEventJsonContent(COMPLETE_CHILD, null)
                .thenVerifyWfRun(wfRun -> {
                    ThreadRun child = wfRun.getThreadRuns(1);
                    Assertions.assertThat(child.getStatus()).isEqualTo(LHStatus.COMPLETED);
                    Assertions.assertThat(child.getParentThreadId()).isEqualTo(0);
                    
                    ThreadRun parent = wfRun.getThreadRuns(0);
                    Assertions.assertThat(parent.getCurrentNodePosition()).isEqualTo(3);
                })
                .thenVerifyNodeRun(0, 3, nodeRun -> {
                    Assertions.assertThat(nodeRun.getStatus()).isEqualTo(LHStatus.RUNNING);
                })
                .thenSendExternalEventJsonContent(COMPLETE_PARENT, null)
                .thenVerifyWfRun(wfRun -> {
                    Assertions.assertThat(wfRun.getStatus()).isEqualTo(LHStatus.COMPLETED);
                })
                .start();
    }

    @Test
    void shouldCompleteWhenParentInterrupted() {
        verifier.prepareRun(interruptAndChildThreadWf)
                .waitForThreadRunStatus(1, LHStatus.RUNNING)
                .thenSendExternalEventJsonContent(PARENT_INTERRUPT_TRIGGER, null)
                .thenVerifyWfRun(wfRun -> {
                    // Ensure that the WfRun is properly interrupted
                    ThreadRun parent = wfRun.getThreadRuns(0);
                    Assertions.assertThat(parent.getStatus()).isEqualTo(LHStatus.HALTED);

                    // Because the parent is halted, the child should be too
                    ThreadRun child = wfRun.getThreadRuns(1);
                    Assertions.assertThat(child.getStatus()).isEqualTo(LHStatus.HALTED);
                    Assertions.assertThat(child.getHaltReasonsCount()).isEqualTo(1);
                    ThreadHaltReason haltReason = child.getHaltReasons(0);
                    Assertions.assertThat(haltReason.getReasonCase()).isEqualTo(ReasonCase.PARENT_HALTED);

                    // And we should have an interrupt handler running.
                    ThreadRun interruptHandler = wfRun.getThreadRuns(2);
                    Assertions.assertThat(interruptHandler.getType()).isEqualTo(ThreadType.INTERRUPT);
                })
                // Child nodeRun should be halted.
                .thenVerifyNodeRun(1, 1, extEvtNodeRun -> {
                    // The "Wait for event" nodeRun in the *CHILD* should be HALTED since
                    // we interrupted the child
                    Assertions.assertThat(extEvtNodeRun.getStatus()).isEqualTo(LHStatus.HALTED);
                })
                // Parent nodeRun should also be halted.
                .thenVerifyNodeRun(0, 2, waitForThreadsRun -> {
                    // The "Wait for event" nodeRun in the *CHILD* should be HALTED since
                    // we interrupted the child
                    Assertions.assertThat(waitForThreadsRun.getStatus()).isEqualTo(LHStatus.HALTED);
                })
                // Wait for interrupt to complete
                .waitForThreadRunStatus(2, LHStatus.COMPLETED)
                .thenSendExternalEventJsonContent(COMPLETE_CHILD, null)
                .thenVerifyWfRun(wfRun -> {
                    ThreadRun child = wfRun.getThreadRuns(1);
                    Assertions.assertThat(child.getStatus()).isEqualTo(LHStatus.COMPLETED);
                    Assertions.assertThat(child.getParentThreadId()).isEqualTo(0);
                    
                    ThreadRun parent = wfRun.getThreadRuns(0);
                    Assertions.assertThat(parent.getCurrentNodePosition()).isEqualTo(3);
                })
                .thenVerifyNodeRun(0, 3, nodeRun -> {
                    Assertions.assertThat(nodeRun.getStatus()).isEqualTo(LHStatus.RUNNING);
                })
                .thenSendExternalEventJsonContent(COMPLETE_PARENT, null)
                .thenVerifyWfRun(wfRun -> {
                    Assertions.assertThat(wfRun.getStatus()).isEqualTo(LHStatus.COMPLETED);
                })
                .start();
    }

    @Test
    void shouldCompleteWhenBothInterruptedChildInterruptedFirst() {
        verifier.prepareRun(interruptAndChildThreadWf)
                .waitForThreadRunStatus(1, LHStatus.RUNNING)
                // Interrupt both of them
                .thenSendExternalEventJsonContent(CHILD_INTERRUPT_TRIGGER, null)
                .thenSendExternalEventJsonContent(PARENT_INTERRUPT_TRIGGER, null)
                .thenVerifyWfRun(wfRun -> {
                    // Ensure that the WfRun is properly interrupted
                    ThreadRun parent = wfRun.getThreadRuns(0);
                    ThreadRun child = wfRun.getThreadRuns(1);
                    ThreadRun childInterrupt = wfRun.getThreadRuns(2);
                    ThreadRun parentInterrupt = wfRun.getThreadRuns(3);

                    Assertions.assertThat(parent.getStatus()).isEqualTo(LHStatus.HALTED);
                    Assertions.assertThat(child.getStatus()).isEqualTo(LHStatus.HALTED);

                    // Child should have TWO halt reasons
                    Assertions.assertThat(child.getHaltReasonsCount()).isEqualTo(2);
                    Assertions.assertThat(child.getHaltReasons(0).getReasonCase()).isEqualTo(ReasonCase.INTERRUPTED);
                    Assertions.assertThat(child.getHaltReasons(1).getReasonCase()).isEqualTo(ReasonCase.PARENT_HALTED);

                    // Verify proper parenthood of interrupt threads
                    Assertions.assertThat(childInterrupt.getParentThreadId()).isEqualTo(1);
                    Assertions.assertThat(parentInterrupt.getParentThreadId()).isEqualTo(0);

                    // Since the Parent Thread is halted, the child is halted, which means the child
                    // interrupt thread (which is a child of the child) should also be halted.
                    Assertions.assertThat(childInterrupt.getStatus()).isEqualTo(LHStatus.HALTED);
                    Assertions.assertThat(childInterrupt.getHaltReasons(0).getReasonCase()).isEqualTo(ReasonCase.PARENT_HALTED);
                })
                // Wait for the interrupt on the parent to complete.
                .waitForThreadRunStatus(3, LHStatus.COMPLETED)
                .thenVerifyWfRun(wfRun -> {
                    // The second interrupt (the parent interrupt) is completed, so the
                    // parent thread should be running again
                    Assertions.assertThat(wfRun.getThreadRuns(0).getStatus()).isEqualTo(LHStatus.RUNNING);

                    // The child interrupt should no longer be halted.
                    ThreadRun childInterruptThread = wfRun.getThreadRuns(2);
                    Assertions.assertThat(childInterruptThread.getStatus()).isNotEqualTo(LHStatus.HALTED);
                })
                .waitForStatus(LHStatus.COMPLETED)
                .start();
    }

    @LHWorkflow("interrupt-and-child-thread")
    public Workflow getInterruptAndChildThreadWf() {
        return Workflow.newWorkflow("interrupt-and-child-thread", parent -> {
            SpawnedThread childHandle = parent.spawnThread(child -> {
                child.waitForEvent(COMPLETE_CHILD);

                child.registerInterruptHandler(CHILD_INTERRUPT_TRIGGER, childInterrupt -> {
                    childInterrupt.execute("iact-dummy");
                });
            }, CHILD_INTERRUPT_TRIGGER, Map.of());

            parent.waitForThreads(SpawnedThreads.of(childHandle));
            parent.waitForEvent(COMPLETE_PARENT);

            parent.registerInterruptHandler(PARENT_INTERRUPT_TRIGGER, parentInterrupt -> {
                parentInterrupt.execute("iact-dummy");
            });
        });
    }

    @LHTaskMethod("iact-dummy")
    public String obiwan() {
        return "hello there";
    }
}
