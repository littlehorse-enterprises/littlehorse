package e2e;

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
    void shouldFailIfSpawnTooManyWf() {
        ArrayList<Integer> largeArr = new ArrayList<>();
        for (int i = 0; i < 65; i++) {
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
