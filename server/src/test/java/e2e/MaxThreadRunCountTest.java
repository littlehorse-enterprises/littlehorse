package e2e;

import io.littlehorse.common.LHConstants;
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

    @Test
    void shouldNotFailIfSpawnFewWf() {
        // Creates Array with MAX_THREAD_RUNS_PER_WF_RUN-1 items, meaning MAX_THREAD_RUNS_PER_WF_RUN-1 new threads (+1
        // entrypoint) which should be the limit, but no error
        ArrayList<Integer> largeArr = new ArrayList<>();
        for (int i = 0; i < LHConstants.MAX_THREAD_RUNS_PER_WF_RUN - 1; i++) {
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
        for (int i = 0; i < LHConstants.MAX_THREAD_RUNS_PER_WF_RUN; i++) {
            largeArr.add(i);
        }

        verifier.prepareRun(spawnManyThreadsWf, Arg.of("json-arr", largeArr))
                .waitForStatus(LHStatus.ERROR)
                .thenVerifyNodeRun(0, 1, nodeRun -> {
                    Assertions.assertThat(nodeRun.getStatus()).isEqualTo(LHStatus.HALTING);
                    Assertions.assertThat(nodeRun.getFailuresList().get(0).getMessage())
                            .contains("You exceeded the maximum number of ThreadRuns per WfRun");
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
