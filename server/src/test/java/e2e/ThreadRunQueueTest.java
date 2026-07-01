package e2e;

import static org.assertj.core.api.Assertions.assertThat;

import io.littlehorse.common.LHServerConfig;
import io.littlehorse.sdk.common.proto.InactiveThreadRun;
import io.littlehorse.sdk.common.proto.InactiveThreadRunId;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
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

@LHTest(externalEventNames = {ThreadRunQueueTest.CONTINUE_EVENT})
public class ThreadRunQueueTest {

    public static final String CONTINUE_EVENT = "thread-run-queue-continue";

    @LHWorkflow("thread-run-queue-wf")
    private Workflow threadRunQueueWf;

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
        int expectedQueuedThreadRuns = totalChildren - (maxThreadRuns - 1);

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
                    assertThat(parked.getThreadRun().getNumber()).isEqualTo(totalChildren);
                    assertThat(parked.getThreadRun().getStatus()).isEqualTo(LHStatus.STARTING);
                });

        // Release every child. As each active child completes and is archived, a queued ThreadRun is
        // dequeued and started, until the queue drains and the WfRun completes.
        for (int i = 0; i < totalChildren; i++) {
            run = run.thenSendExternalEventWithContent(CONTINUE_EVENT, Map.of());
        }

        run.waitForStatus(LHStatus.COMPLETED)
                .thenVerifyWfRun(wfRun -> {
                    assertThat(wfRun.getThreadRunQueueCount()).isZero();
                    assertThat(wfRun.getGreatestThreadrunNumber()).isEqualTo(totalChildren);
                })
                .start();
    }

    @LHWorkflow("thread-run-queue-wf")
    public Workflow buildThreadRunQueueWf() {
        return Workflow.newWorkflow("thread-run-queue-wf", wf -> {
            WfRunVariable arr = wf.declareJsonArr("json-arr").required();

            var spawnedThreads =
                    wf.spawnThreadForEach(arr, "queued-child", child -> child.waitForEvent(CONTINUE_EVENT));

            wf.waitForThreads(spawnedThreads);
        });
    }
}
