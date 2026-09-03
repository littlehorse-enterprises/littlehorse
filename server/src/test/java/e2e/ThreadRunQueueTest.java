package e2e;

import static org.assertj.core.api.Assertions.assertThat;

import io.littlehorse.common.LHServerConfig;
import io.littlehorse.sdk.common.proto.InactiveThreadRun;
import io.littlehorse.sdk.common.proto.InactiveThreadRunId;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.ResumeWfRunRequest;
import io.littlehorse.sdk.common.proto.StopWfRunRequest;
import io.littlehorse.sdk.common.proto.VariableMutationType;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.util.Arg;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.LHWorkflow;
import io.littlehorse.test.WfRunVerifier;
import io.littlehorse.test.WorkflowVerifier;
import java.util.ArrayList;
import java.util.Map;
import org.junit.jupiter.api.Test;

@LHTest(
        externalEventNames = {
            ThreadRunQueueTest.CONTINUE_EVENT,
            ThreadRunQueueTest.FINISH_EVENT,
            ThreadRunQueueTest.FAIL_EVENT
        })
public class ThreadRunQueueTest {

    public static final String CONTINUE_EVENT = "thread-run-queue-continue";
    public static final String FINISH_EVENT = "thread-run-queue-finish";
    public static final String FAIL_EVENT = "thread-run-queue-fail";

    @LHWorkflow("thread-run-queue-wf")
    private Workflow threadRunQueueWf;

    @LHWorkflow("thread-run-queue-parent-failure-wf")
    private Workflow parentFailureWf;

    private WorkflowVerifier verifier;
    private LittleHorseBlockingStub lhClient;
    private LHServerConfig serverConfig = new LHServerConfig();

    @Test
    void shouldQueueExcessThreadRunsThenDequeueThemUntilCompletion() {
        int maxThreadRuns = serverConfig.getMaxThreadRunsPerWfRun();

        // Spawn more child ThreadRuns than the WfRun is allowed to run concurrently. The entrypoint
        // ThreadRun (number 0) occupies one slot, so (maxThreadRuns - 1) children can run at once and
        // the rest must be parked in the ThreadRun queue.
        int totalChildren = maxThreadRuns + 5;
        int expectedActiveThreadRuns = maxThreadRuns;
        int expectedQueuedThreadRuns = 6;

        ArrayList<Integer> childArr = new ArrayList<>();
        for (int i = 0; i < totalChildren; i++) {
            childArr.add(i);
        }

        WfRunVerifier run = verifier.prepareRun(threadRunQueueWf, Arg.of("json-arr", childArr))
                .waitForStatus(LHStatus.RUNNING)
                // Every active child blocks on CONTINUE_EVENT, so the in-memory ThreadRuns stay at the
                // cap while the overflow children are parked in the queue.
                .thenVerifyWfRun(wfRun -> {
                    assertThat(wfRun.getThreadRunsCount()).isEqualTo(expectedActiveThreadRuns);
                    assertThat(wfRun.getThreadRunQueueCount()).isEqualTo(expectedQueuedThreadRuns);
                    assertThat(wfRun.getGreatestThreadrunNumber()).isEqualTo(totalChildren);
                })
                // A parked ThreadRun is persisted as an InactiveThreadRun of type QUEUED.
                .thenVerifyWfRun(wfRun -> {
                    InactiveThreadRun parked = lhClient.getInactiveThreadRun(InactiveThreadRunId.newBuilder()
                            .setWfRunId(wfRun.getId())
                            .setThreadRunNumber(totalChildren)
                            .build());
                    assertThat(parked.hasQueued()).isTrue();
                });

        // Release every child. As each active child completes and is archived, a queued ThreadRun is
        // dequeued and started, until the queue drains and the WfRun completes.
        for (int i = 0; i < totalChildren; i++) {
            run = run.thenSendExternalEventWithContent(CONTINUE_EVENT, Map.of());
        }

        run.thenSendExternalEventWithContent(FINISH_EVENT, Map.of())
                .waitForStatus(LHStatus.COMPLETED)
                .thenVerifyWfRun(wfRun -> {
                    assertThat(wfRun.getThreadRunQueueCount()).isZero();
                })
                .start();
    }

    @Test
    void shouldNotDequeueHaltedThreadRun() {
        int maxThreadRuns = serverConfig.getMaxThreadRunsPerWfRun();
        int queuedThreadRunNumber = maxThreadRuns;

        ArrayList<Integer> childArr = new ArrayList<>();
        for (int i = 0; i < maxThreadRuns; i++) {
            childArr.add(i);
        }

        verifier.prepareRun(threadRunQueueWf, Arg.of("json-arr", childArr))
                .waitForStatus(LHStatus.RUNNING)
                .thenVerifyWfRun(wfRun -> {
                    assertThat(wfRun.getThreadRunQueueList()).containsExactly(queuedThreadRunNumber);
                    lhClient.stopWfRun(StopWfRunRequest.newBuilder()
                            .setWfRunId(wfRun.getId())
                            .setThreadRunNumber(queuedThreadRunNumber)
                            .build());
                })
                .thenSendExternalEventWithContent(CONTINUE_EVENT, Map.of())
                .thenVerifyWfRun(wfRun -> {
                    assertThat(wfRun.getThreadRunQueueList()).containsExactly(queuedThreadRunNumber);

                    InactiveThreadRun queued = lhClient.getInactiveThreadRun(InactiveThreadRunId.newBuilder()
                            .setWfRunId(wfRun.getId())
                            .setThreadRunNumber(queuedThreadRunNumber)
                            .build());
                    assertThat(queued.hasQueued()).isTrue();
                    assertThat(queued.getThreadRun().getStatus()).isEqualTo(LHStatus.HALTED);
                    assertThat(queued.getThreadRun().getHaltReasonsList()).anyMatch(reason -> reason.hasManualHalt());
                })
                .start();
    }

    @Test
    void shouldDequeueNextThreadRunWhenQueueHeadIsHalted() {
        int maxThreadRuns = serverConfig.getMaxThreadRunsPerWfRun();
        int haltedThreadRunNumber = maxThreadRuns;
        int nextThreadRunNumber = maxThreadRuns + 1;

        ArrayList<Integer> childArr = new ArrayList<>();
        for (int i = 0; i < maxThreadRuns + 1; i++) {
            childArr.add(i);
        }

        verifier.prepareRun(threadRunQueueWf, Arg.of("json-arr", childArr))
                .waitForStatus(LHStatus.RUNNING)
                .thenVerifyWfRun(wfRun -> {
                    assertThat(wfRun.getThreadRunQueueList())
                            .containsExactly(haltedThreadRunNumber, nextThreadRunNumber);
                    lhClient.stopWfRun(StopWfRunRequest.newBuilder()
                            .setWfRunId(wfRun.getId())
                            .setThreadRunNumber(haltedThreadRunNumber)
                            .build());
                })
                .thenSendExternalEventWithContent(CONTINUE_EVENT, Map.of())
                .thenVerifyWfRun(wfRun -> {
                    assertThat(wfRun.getThreadRunQueueList()).containsExactly(haltedThreadRunNumber);
                    assertThat(wfRun.getThreadRunsList())
                            .filteredOn(threadRun -> threadRun.getNumber() == nextThreadRunNumber)
                            .singleElement()
                            .satisfies(threadRun ->
                                    assertThat(threadRun.getStatus()).isEqualTo(LHStatus.RUNNING));

                    InactiveThreadRun halted = lhClient.getInactiveThreadRun(InactiveThreadRunId.newBuilder()
                            .setWfRunId(wfRun.getId())
                            .setThreadRunNumber(haltedThreadRunNumber)
                            .build());
                    assertThat(halted.getThreadRun().getStatus()).isEqualTo(LHStatus.HALTED);
                })
                .start();
    }

    @Test
    void shouldDequeueQueuedThreadRunAfterParentHaltReasonResolves() {
        int maxThreadRuns = serverConfig.getMaxThreadRunsPerWfRun();
        int queuedThreadRunNumber = maxThreadRuns;

        ArrayList<Integer> childArr = new ArrayList<>();
        for (int i = 0; i < maxThreadRuns; i++) {
            childArr.add(i);
        }

        verifier.prepareRun(threadRunQueueWf, Arg.of("json-arr", childArr))
                .waitForStatus(LHStatus.RUNNING)
                .thenVerifyWfRun(wfRun -> {
                    assertThat(wfRun.getThreadRunQueueList()).containsExactly(queuedThreadRunNumber);
                    lhClient.stopWfRun(StopWfRunRequest.newBuilder()
                            .setWfRunId(wfRun.getId())
                            .setThreadRunNumber(0)
                            .build());
                })
                .thenVerifyWfRun(wfRun -> {
                    InactiveThreadRun queued = lhClient.getInactiveThreadRun(InactiveThreadRunId.newBuilder()
                            .setWfRunId(wfRun.getId())
                            .setThreadRunNumber(queuedThreadRunNumber)
                            .build());
                    assertThat(queued.getThreadRun().getStatus()).isEqualTo(LHStatus.HALTED);
                    assertThat(queued.getThreadRun().getHaltReasonsList())
                            .singleElement()
                            .satisfies(reason ->
                                    assertThat(reason.hasParentHalted()).isTrue());
                })
                .thenVerifyWfRun(wfRun -> lhClient.resumeWfRun(ResumeWfRunRequest.newBuilder()
                        .setWfRunId(wfRun.getId())
                        .setThreadRunNumber(0)
                        .build()))
                .thenSendExternalEventWithContent(CONTINUE_EVENT, Map.of())
                .thenVerifyWfRun(wfRun -> {
                    assertThat(wfRun.getThreadRunQueueList()).doesNotContain(queuedThreadRunNumber);
                    assertThat(wfRun.getThreadRunsList())
                            .filteredOn(threadRun -> threadRun.getNumber() == queuedThreadRunNumber)
                            .singleElement()
                            .satisfies(threadRun ->
                                    assertThat(threadRun.getStatus()).isEqualTo(LHStatus.RUNNING));
                })
                .start();
    }

    @Test
    void shouldHaltQueuedThreadRunWhenParentFails() {
        int maxThreadRuns = serverConfig.getMaxThreadRunsPerWfRun();
        int queuedThreadRunNumber = maxThreadRuns;

        ArrayList<Integer> childArr = new ArrayList<>();
        for (int i = 0; i < maxThreadRuns; i++) {
            childArr.add(i);
        }

        verifier.prepareRun(parentFailureWf, Arg.of("json-arr", childArr))
                .waitForStatus(LHStatus.RUNNING)
                .thenVerifyWfRun(
                        wfRun -> assertThat(wfRun.getThreadRunQueueList()).containsExactly(queuedThreadRunNumber))
                .thenSendExternalEventWithContent(FAIL_EVENT, "not-an-integer")
                .waitForStatus(LHStatus.ERROR)
                .thenVerifyWfRun(wfRun -> {
                    assertThat(wfRun.getThreadRunQueueList()).containsExactly(queuedThreadRunNumber);

                    InactiveThreadRun queued = lhClient.getInactiveThreadRun(InactiveThreadRunId.newBuilder()
                            .setWfRunId(wfRun.getId())
                            .setThreadRunNumber(queuedThreadRunNumber)
                            .build());
                    assertThat(queued.getThreadRun().getStatus()).isEqualTo(LHStatus.HALTED);
                    assertThat(queued.getThreadRun().getHaltReasonsList())
                            .singleElement()
                            .satisfies(reason ->
                                    assertThat(reason.hasParentHalted()).isTrue());
                })
                .start();
    }

    @LHWorkflow("thread-run-queue-wf")
    public Workflow buildThreadRunQueueWf() {
        return Workflow.newWorkflow("thread-run-queue-wf", wf -> {
            WfRunVariable arr = wf.declareJsonArr("json-arr").required();

            wf.spawnThreadForEach(arr, "queued-child", child -> child.waitForEvent(CONTINUE_EVENT));
            wf.waitForEvent(FINISH_EVENT);
        });
    }

    @LHWorkflow("thread-run-queue-parent-failure-wf")
    public Workflow buildParentFailureWf() {
        return Workflow.newWorkflow("thread-run-queue-parent-failure-wf", wf -> {
            WfRunVariable arr = wf.declareJsonArr("json-arr").required();
            WfRunVariable integer = wf.addVariable("integer", VariableType.INT);

            wf.spawnThreadForEach(arr, "queued-child", child -> child.waitForEvent(CONTINUE_EVENT));
            wf.mutate(integer, VariableMutationType.ASSIGN, wf.waitForEvent(FAIL_EVENT));
        });
    }
}
