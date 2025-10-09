package e2e;

import io.littlehorse.sdk.common.proto.Interrupted;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.ThreadHaltReason;
import io.littlehorse.sdk.common.proto.ThreadHaltReason.ReasonCase;
import io.littlehorse.sdk.common.proto.ThreadRun;
import io.littlehorse.sdk.common.proto.ThreadType;
import io.littlehorse.sdk.wfsdk.SpawnedThread;
import io.littlehorse.sdk.wfsdk.SpawnedThreads;
import io.littlehorse.sdk.wfsdk.WaitForThreadsNodeOutput;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.WorkflowThread;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.LHWorkflow;
import io.littlehorse.test.WorkflowVerifier;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/*
 * Verifies edge cases when interrupting parents and children at the same time.
 */
@LHTest(
        externalEventNames = {
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

    @LHWorkflow("handle-failure-on-interrupt")
    public Workflow handleFailureOnInterruptThreadWf;

    private WorkflowVerifier verifier;

    @Test
    void shouldCompleteWithNoInterrupts() {
        verifier.prepareRun(interruptAndChildThreadWf)
                .waitForThreadRunStatus(1, LHStatus.RUNNING)
                .thenSendExternalEventWithContent(COMPLETE_CHILD, null)
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
                .thenSendExternalEventWithContent(COMPLETE_PARENT, null)
                .thenVerifyWfRun(wfRun -> {
                    Assertions.assertThat(wfRun.getStatus()).isEqualTo(LHStatus.COMPLETED);
                })
                .start();
    }

    @Test
    void shouldCompleteWhenChildInterrupted() {
        verifier.prepareRun(interruptAndChildThreadWf)
                .waitForThreadRunStatus(1, LHStatus.RUNNING)
                .thenSendExternalEventWithContent(CHILD_INTERRUPT_TRIGGER, null)
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
                .thenSendExternalEventWithContent(COMPLETE_CHILD, null)
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
                .thenSendExternalEventWithContent(COMPLETE_PARENT, null)
                .thenVerifyWfRun(wfRun -> {
                    Assertions.assertThat(wfRun.getStatus()).isEqualTo(LHStatus.COMPLETED);
                })
                .start();
    }

    @Test
    void shouldCompleteWhenParentInterrupted() {
        verifier.prepareRun(interruptAndChildThreadWf)
                .waitForThreadRunStatus(1, LHStatus.RUNNING)
                .thenSendExternalEventWithContent(PARENT_INTERRUPT_TRIGGER, null)
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
                .thenSendExternalEventWithContent(COMPLETE_CHILD, null)
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
                .thenSendExternalEventWithContent(COMPLETE_PARENT, null)
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
                .thenSendExternalEventWithContent(CHILD_INTERRUPT_TRIGGER, null)
                .thenSendExternalEventWithContent(PARENT_INTERRUPT_TRIGGER, null)
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
                    Assertions.assertThat(child.getHaltReasons(0).getReasonCase())
                            .isEqualTo(ReasonCase.INTERRUPTED);
                    Assertions.assertThat(child.getHaltReasons(1).getReasonCase())
                            .isEqualTo(ReasonCase.PARENT_HALTED);

                    // Verify proper parenthood of interrupt threads
                    Assertions.assertThat(childInterrupt.getParentThreadId()).isEqualTo(1);
                    Assertions.assertThat(parentInterrupt.getParentThreadId()).isEqualTo(0);

                    // Since the Parent Thread is halted, the child is halted, which means the child
                    // interrupt thread (which is a child of the child) should also be halted or halting
                    // depending on when the taskrun finishes.
                    Assertions.assertThat(childInterrupt.getStatus()).isIn(LHStatus.HALTED, LHStatus.HALTING);
                    Assertions.assertThat(childInterrupt.getHaltReasons(0).getReasonCase())
                            .isEqualTo(ReasonCase.PARENT_HALTED);
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
                .thenSendExternalEventWithContent(COMPLETE_CHILD, "asdf")
                .thenSendExternalEventWithContent(COMPLETE_PARENT, "asdf")
                .waitForStatus(LHStatus.COMPLETED)
                .start();
    }

    @Test // handle-failure-on-interrupt
    public void shouldHandleFailureOnInterruptThreads() {
        verifier.prepareRun(handleFailureOnInterruptThreadWf)
                .waitForStatus(LHStatus.RUNNING)
                .thenSendExternalEventWithContent(CHILD_INTERRUPT_TRIGGER, null)
                .waitForStatus(LHStatus.COMPLETED)
                .thenVerifyWfRun(wfRun -> {
                    Assertions.assertThat(wfRun.getThreadRunsList()).hasSize(4);
                    ThreadRun entrypointThread = wfRun.getThreadRunsList().get(0);
                    ThreadRun childThread = wfRun.getThreadRunsList().get(1);
                    ThreadRun interruptThread = wfRun.getThreadRunsList().get(2);
                    Assertions.assertThat(entrypointThread.getStatus()).isEqualTo(LHStatus.COMPLETED);
                    Assertions.assertThat(childThread.getStatus()).isEqualTo(LHStatus.EXCEPTION);
                    Assertions.assertThat(childThread.getErrorMessage())
                            .isEqualTo("Interrupt thread with id 2 failed!");
                    Assertions.assertThat(interruptThread.getStatus()).isEqualTo(LHStatus.EXCEPTION);
                })
                .start();
    }

    @LHWorkflow("interrupt-and-child-thread")
    public Workflow getInterruptAndChildThreadWf() {
        return Workflow.newWorkflow("interrupt-and-child-thread", parent -> {
            SpawnedThread childHandle = parent.spawnThread(
                    child -> {
                        child.waitForEvent(COMPLETE_CHILD);

                        child.registerInterruptHandler(CHILD_INTERRUPT_TRIGGER, childInterrupt -> {
                            childInterrupt.execute("iact-dummy");
                        });
                    },
                    "child-thread",
                    Map.of());

            parent.waitForThreads(SpawnedThreads.of(childHandle));
            parent.waitForEvent(COMPLETE_PARENT);

            parent.registerInterruptHandler(PARENT_INTERRUPT_TRIGGER, parentInterrupt -> {
                parentInterrupt.execute("iact-dummy");
            });
        });
    }

    @LHWorkflow("handle-failure-on-interrupt")
    public Workflow getHandleFailureOnInterruptThreadWf() {
        return Workflow.newWorkflow("handle-failure-on-interrupt", parent -> {
            SpawnedThread childHandle = parent.spawnThread(
                    child -> {
                        child.sleepSeconds(1);

                        child.registerInterruptHandler(CHILD_INTERRUPT_TRIGGER, childInterrupt -> {
                            childInterrupt.fail("i-fail", "failed");
                        });
                    },
                    "child-thread",
                    Map.of());

            WaitForThreadsNodeOutput waitForThreads = parent.waitForThreads(SpawnedThreads.of(childHandle));

            parent.handleAnyFailure(waitForThreads, WorkflowThread::complete);
        });
    }

    @LHTaskMethod("iact-dummy")
    public String obiwan() {
        try {
            Thread.sleep(500);
        } catch (Exception exn) {
        }
        return "hello there";
    }
}
