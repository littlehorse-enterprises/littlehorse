package e2e;

import static org.assertj.core.api.Assertions.assertThat;

import io.grpc.Status.Code;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.NodeRun.NodeTypeCase;
import io.littlehorse.sdk.common.proto.TaskRun;
import io.littlehorse.sdk.common.proto.TaskStatus;
import io.littlehorse.sdk.common.proto.Operation;
import io.littlehorse.sdk.common.util.Arg;
import io.littlehorse.sdk.wfsdk.SpawnedThread;
import io.littlehorse.sdk.wfsdk.SpawnedThreads;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.sdk.worker.WorkerContext;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.LHWorkflow;
import io.littlehorse.test.WorkflowVerifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.Test;

@LHTest(externalEventNames = {"rescue-complete-child", "rescue-complete-parent"})
public class RescueThreadRunTest {

    private WorkflowVerifier verifier;

    private static Map<String, Integer> wfRunToTimesFailed = new ConcurrentHashMap<>();

    @LHWorkflow("simple-rescue-threadrun")
    private Workflow simpleRescueThreadRun;

    @LHWorkflow("multi-thread-rescue-threadrun")
    private Workflow multiThreadRescueThreadRun;

    @LHWorkflow("simple-rescue-threadrun")
    public Workflow getSimpleWorkflow() {
        return Workflow.newWorkflow("simple-rescue-threadrun", wf -> {
            WfRunVariable timesToFail = wf.addVariable("times-to-fail", 1);
            WfRunVariable didMutationHappen = wf.addVariable("did-mutation-happen", false);

            // Fail a configurable amount of times
            wf.execute("throw-error-x-times", timesToFail);
            wf.mutate(didMutationHappen, Operation.ASSIGN, true);

            // Should continue on for free
            wf.execute("no-rescue-needed");
        });
    }

    // This test allows us to test edge cases about starting and resuming failed child/parent threadruns
    // when doing the RescueThreadRun RPC.
    @LHWorkflow("multi-thread-rescue-threadrun")
    public Workflow getMultiThreadWorkflow() {
        return Workflow.newWorkflow("multi-thread-rescue-threadrun", wf -> {

            // Allow us to control which threadRun fails and when
            WfRunVariable parentFailures = wf.addVariable("parent-failures", 0);
            WfRunVariable childFailures = wf.addVariable("child-failures", 0);

            SpawnedThread childThreadHandle = wf.spawnThread(
                    child -> {
                        child.execute("throw-error-x-times", childFailures);

                        // WaitForEvent allows us to control when each one completes
                        child.waitForEvent("rescue-complete-child");
                    },
                    "child",
                    null);

            wf.execute("throw-error-x-times", parentFailures);
            wf.waitForEvent("rescue-complete-parent");

            wf.waitForThreads(SpawnedThreads.of(childThreadHandle));
        });
    }

    @Test
    void shouldExecuteFailedNodeAgainOnRescue() {
        // If we execute the failed Node again, we *SHOULD* observe the mutations.
        verifier.prepareRun(simpleRescueThreadRun, Arg.of("times-to-fail", 1))
                .waitForNodeRunStatus(0, 1, LHStatus.ERROR)
                .thenRescueThreadRun(0, false) // dont skip failed node: try again
                .waitForStatus(LHStatus.COMPLETED)
                .thenVerifyAllTaskRuns(unsortedTaskRuns -> {
                    List<TaskRun> taskRuns = new ArrayList<>(unsortedTaskRuns);

                    taskRuns.sort((task1, task2) -> {
                        return task1.getSource().getTaskNode().getNodeRunId().getPosition()
                                - task2.getSource().getTaskNode().getNodeRunId().getPosition();
                    });

                    assertThat(taskRuns.size()).isEqualTo(3);
                    assertThat(taskRuns.get(0).getStatus()).isEqualTo(TaskStatus.TASK_FAILED);
                    assertThat(taskRuns.get(1).getTaskDefId().getName()).isEqualTo("throw-error-x-times");
                    assertThat(taskRuns.get(2).getTaskDefId().getName()).isEqualTo("no-rescue-needed");
                })
                .thenVerifyVariable(0, "did-mutation-happen", variable -> {
                    assertThat(variable.getBool()).isTrue();
                })
                .start();
    }

    @Test
    void shouldSkipFailedNodeRunOnRescue() {
        verifier.prepareRun(simpleRescueThreadRun, Arg.of("times-to-fail", 1))
                .waitForNodeRunStatus(0, 1, LHStatus.ERROR)
                .thenRescueThreadRun(0, true) // skip failed node
                .waitForStatus(LHStatus.COMPLETED)
                .thenVerifyAllTaskRuns(unsortedTaskRuns -> {
                    List<TaskRun> taskRuns = new ArrayList<>(unsortedTaskRuns);

                    taskRuns.sort((task1, task2) -> {
                        return task1.getSource().getTaskNode().getNodeRunId().getPosition()
                                - task2.getSource().getTaskNode().getNodeRunId().getPosition();
                    });

                    assertThat(taskRuns.size()).isEqualTo(2);
                    assertThat(taskRuns.get(0).getStatus()).isEqualTo(TaskStatus.TASK_FAILED);
                    assertThat(taskRuns.get(1).getTaskDefId().getName()).isEqualTo("no-rescue-needed");
                })
                .thenVerifyVariable(0, "did-mutation-happen", variable -> {
                    assertThat(variable.getBool()).isFalse();
                })
                .start();
    }

    @Test
    void cantRescueIfThreadIsntError() {
        verifier.prepareRun(simpleRescueThreadRun, Arg.of("times-to-fail", 0)) // don't fail at all
                .waitForNodeRunStatus(0, 1, LHStatus.COMPLETED)
                .thenRescueThreadRun(0, true, exn -> {
                    assertThat(exn.getStatus().getCode()).isEqualTo(Code.FAILED_PRECONDITION);
                })
                .waitForStatus(LHStatus.COMPLETED)
                .start();
    }

    @Test
    void childThreadRestartsIfParentIsRescued() {
        verifier.prepareRun(multiThreadRescueThreadRun, Arg.of("parent-failures", 1))
                .waitForNodeRunStatus(0, 2, LHStatus.ERROR)
                .waitForThreadRunStatus(1, LHStatus.HALTED)
                .thenRescueThreadRun(0, false)
                .thenSendExternalEventWithContent("rescue-complete-parent", "Ahsoka Tano")
                .thenSendExternalEventWithContent("rescue-complete-child", "Ima-Gun Di")
                .waitForStatus(LHStatus.COMPLETED)
                .thenVerifyWfRun(wfRun -> {
                    assertThat(wfRun.getThreadRuns(1).getStatus()).isEqualTo(LHStatus.COMPLETED);
                })
                .start();
    }

    @Test
    void parentThreadRestartsFromWaitForThreadsNodeIfChildIsRescued() {
        verifier.prepareRun(multiThreadRescueThreadRun, Arg.of("child-failures", 1))
                .thenSendExternalEventWithContent("rescue-complete-parent", "Obi-Wan Kenobi")
                .waitForNodeRunStatus(1, 1, LHStatus.ERROR)
                // Parent will fail once child fails
                .waitForThreadRunStatus(0, LHStatus.ERROR)
                .thenVerifyWfRun(wfRun -> {
                    assertThat(wfRun.getThreadRuns(0).getCurrentNodePosition()).isEqualTo(4);
                })
                // We should be on a WAIT_FOR_THREADS node right now
                .thenVerifyNodeRun(0, 4, nodeRun -> {
                    assertThat(nodeRun.getNodeTypeCase()).isEqualTo(NodeTypeCase.WAIT_FOR_THREADS);
                    assertThat(nodeRun.getStatus()).isEqualTo(LHStatus.ERROR);
                })
                .thenRescueThreadRun(1, false)
                .thenSendExternalEventWithContent("rescue-complete-child", "Kit Fisto")
                .waitForStatus(LHStatus.COMPLETED)
                // The failed WAIT_FOR_THREADS NodeRun should remain as a historical artifact.
                // What we want to do is verify that there was a *SECOND* attempt at waiting
                // for threads.
                .thenVerifyNodeRun(0, 5, nodeRun -> {
                    assertThat(nodeRun.getNodeTypeCase()).isEqualTo(NodeTypeCase.WAIT_FOR_THREADS);
                })
                .start();
    }

    @Test
    void parentThreadOnExternalEventNodeNotAffectedByChildRescue() {
        verifier.prepareRun(multiThreadRescueThreadRun, Arg.of("child-failures", 1))
                .waitForNodeRunStatus(1, 1, LHStatus.ERROR)
                // Parent should still be RUNNING but child is ERROR
                .thenVerifyWfRun(wfRun -> {
                    assertThat(wfRun.getThreadRuns(0).getStatus()).isEqualTo(LHStatus.RUNNING);
                    assertThat(wfRun.getThreadRuns(0).getCurrentNodePosition()).isEqualTo(3);
                    assertThat(wfRun.getThreadRuns(1).getStatus()).isEqualTo(LHStatus.ERROR);
                })
                .thenRescueThreadRun(1, false)
                .thenSendExternalEventWithContent("rescue-complete-child", "Agen Kolar")
                .thenSendExternalEventWithContent("rescue-complete-parent", "Saesee Tiin")
                .waitForStatus(LHStatus.COMPLETED)
                // We should only have ONE wait for threads node on the parent.
                .thenVerifyNodeRun(0, 5, nodeRun -> {
                    assertThat(nodeRun.getNodeTypeCase()).isEqualTo(NodeTypeCase.EXIT);
                })
                .start();
    }

    @LHTaskMethod("no-rescue-needed")
    public void noRescueNeeded() {
        // nothing to do
    }

    @LHTaskMethod("throw-error-x-times")
    public void throwErrorXTimes(int timesToFail, WorkerContext ctx) {
        String key = ctx.getWfRunId().getId() + "-" + ctx.getNodeRunId().getThreadRunNumber();
        int timesFailed = wfRunToTimesFailed.computeIfAbsent(key, (k) -> {
            return 0;
        });
        if (timesFailed < timesToFail) {
            wfRunToTimesFailed.put(key, timesFailed + 1);
            throw new RuntimeException("hahaha");
        }
    }
}
