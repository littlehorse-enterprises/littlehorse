package e2e;

import io.littlehorse.common.LHServerConfig;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.util.Arg;
import io.littlehorse.sdk.wfsdk.SpawnedThreads;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.LHWorkflow;
import io.littlehorse.test.WorkflowVerifier;
import java.util.ArrayList;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

@LHTest
public class MaxThreadRunCountTest {
    @LHWorkflow("spawn-many-threads-wf")
    private Workflow spawnManyThreadsWf;

    private WorkflowVerifier verifier;
    private LHServerConfig serverConfig = new LHServerConfig();

    @Test
    void shouldNotFailIfSpawnFewWf() {
        // Creates Array with MAX_THREAD_RUNS_PER_WF_RUN-1 items, meaning MAX_THREAD_RUNS_PER_WF_RUN-1 new threads (+1
        // entrypoint) which should be the limit, but no error
        ArrayList<Integer> largeArr = new ArrayList<>();
        for (int i = 0; i < this.serverConfig.getMaxThreadRunsPerWfRun() - 1; i++) {
            largeArr.add(i);
        }

        verifier.prepareRun(spawnManyThreadsWf, Arg.of("json-arr", largeArr))
                .waitForStatus(LHStatus.COMPLETED)
                .start();
    }

    @Test
    void shouldFailIfSpawnTooManyWf() {
        // Creates Array with MAX_THREAD_RUNS_PER_WF_RUN items, meaning MAX_THREAD_RUNS_PER_WF_RUN new threads (+1
        // entrypoint) which will throw error
        ArrayList<Integer> largeArr = new ArrayList<>();
        for (int i = 0; i < this.serverConfig.getMaxThreadRunsPerWfRun(); i++) {
            largeArr.add(i);
        }

        verifier.prepareRun(spawnManyThreadsWf, Arg.of("json-arr", largeArr))
                .waitForStatus(LHStatus.ERROR)
                .thenVerifyNodeRun(0, 1, nodeRun -> {
                    Assertions.assertThat(nodeRun.getStatus()).isEqualTo(LHStatus.ERROR);
                    Assertions.assertThat(nodeRun.getFailuresList().get(0).getMessage())
                            .contains("exceeding the maximum number of ThreadRuns per WfRun");
                })
                .start();
    }

    @Test
    void shouldNotLeaveOrphanThreadsWhenSpawnExceedsLimit() {
        int maxThreadRuns = this.serverConfig.getMaxThreadRunsPerWfRun();
        ArrayList<Integer> largeArr = new ArrayList<>();
        for (int i = 0; i < maxThreadRuns; i++) {
            largeArr.add(i);
        }

        // 1 entrypoint ThreadRun + maxThreadRuns requested children would exceed the limit.
        String expectedFailureMessage = String.format(
                "WfRun would have %d ThreadRuns, exceeding the maximum number of ThreadRuns per WfRun: %d. "
                        + "Reduce the number of spawned ThreadRuns or increase LHS_X_MAX_THREAD_RUNS_PER_WF_RUN.",
                maxThreadRuns + 1, maxThreadRuns);

        verifier.prepareRun(spawnManyThreadsWf, Arg.of("json-arr", largeArr))
                .waitForStatus(LHStatus.ERROR)
                .thenVerifyNodeRun(0, 1, nodeRun -> {
                    Assertions.assertThat(nodeRun.getStatus()).isEqualTo(LHStatus.ERROR);
                    Assertions.assertThat(nodeRun.getFailuresList().get(0).getMessage())
                            .isEqualTo(expectedFailureMessage);
                })
                .thenVerifyWfRun(wfRun -> {
                    Assertions.assertThat(wfRun.getThreadRunsCount()).isEqualTo(1);
                    Assertions.assertThat(wfRun.getThreadRuns(0).getStatus()).isEqualTo(LHStatus.ERROR);
                })
                .start();
    }

    @LHWorkflow("spawn-many-threads-wf")
    public Workflow spawnNThreadsWf() {
        return new WorkflowImpl("spawn-many-threads-wf", wf -> {
            WfRunVariable arr = wf.declareJsonArr("json-arr").required();

            SpawnedThreads spawnedThreads = wf.spawnThreadForEach(arr, "test-thread", handler -> {
                handler.complete();
            });

            wf.waitForThreads(spawnedThreads);
        });
    }
}
