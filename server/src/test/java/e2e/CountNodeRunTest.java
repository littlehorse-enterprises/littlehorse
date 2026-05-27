package e2e;

import static org.assertj.core.api.Assertions.*;

import io.littlehorse.sdk.common.proto.Count;
import io.littlehorse.sdk.common.proto.CountNodeRunRequest;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc;
import io.littlehorse.sdk.common.proto.RunWfRequest;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.WfSpec;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.LHWorkflow;
import io.littlehorse.test.WorkflowVerifier;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@LHTest
@Tag("slow")
public class CountNodeRunTest {

    private static final String WF_SPEC_NAME = "count-node-run-test";
    private static final int RUNS_PER_VERSION = 3;

    @LHWorkflow(WF_SPEC_NAME)
    private Workflow basic;

    private WorkflowVerifier verifier;
    private LittleHorseGrpc.LittleHorseBlockingStub client;

    // Version tracking: majorVersion 0 revision 0, majorVersion 0 revision 1, majorVersion 1 revision 0
    private final int nodesPerRun = 3; // entrypoint + task + exit = 3

    @BeforeEach
    void setup() {

        Workflow revision0 = Workflow.newWorkflow(WF_SPEC_NAME, wf -> {
            wf.execute("count-node-run-task");
        });
        WfSpec rev0Spec = client.putWfSpec(revision0.compileWorkflow());
        assertThat(rev0Spec.getId().getMajorVersion()).isEqualTo(0);
        assertThat(rev0Spec.getId().getRevision()).isEqualTo(0);

        // Register a minor revision (0/1) by adding an optional variable.
        Workflow revision1 = Workflow.newWorkflow(WF_SPEC_NAME, wf -> {
            wf.execute("count-node-run-task");
            wf.addVariable("optional-var", VariableType.STR);
        });
        WfSpec rev1Spec = client.putWfSpec(revision1.compileWorkflow());
        assertThat(rev1Spec.getId().getMajorVersion()).isEqualTo(0);
        assertThat(rev1Spec.getId().getRevision()).isEqualTo(1);

        // Register a major version (1/0) by adding a required variable.
        Workflow majorVersion1 = Workflow.newWorkflow(WF_SPEC_NAME, wf -> {
            wf.execute("count-node-run-task");
            wf.addVariable("required-var", VariableType.BOOL).required();
        });
        WfSpec major1Spec = client.putWfSpec(majorVersion1.compileWorkflow());
        assertThat(major1Spec.getId().getMajorVersion()).isEqualTo(1);
        assertThat(major1Spec.getId().getRevision()).isEqualTo(0);

        // Run workflows on each version
        // Version 0/0: not directly runnable after revision 1 exists (latest of major 0 is 0/1)
        // Version 0/1: run RUNS_PER_VERSION times
        for (int i = 0; i < RUNS_PER_VERSION; i++) {
            client.runWf(RunWfRequest.newBuilder()
                    .setWfSpecName(WF_SPEC_NAME)
                    .setMajorVersion(0)
                    .setRevision(1)
                    .build());
        }

        // Version 0/0: run RUNS_PER_VERSION times
        for (int i = 0; i < RUNS_PER_VERSION; i++) {
            client.runWf(RunWfRequest.newBuilder()
                    .setWfSpecName(WF_SPEC_NAME)
                    .setMajorVersion(0)
                    .setRevision(0)
                    .build());
        }

        // Version 1/0: run RUNS_PER_VERSION times
        for (int i = 0; i < RUNS_PER_VERSION; i++) {
            client.runWf(RunWfRequest.newBuilder()
                    .setWfSpecName(WF_SPEC_NAME)
                    .setMajorVersion(1)
                    .setRevision(0)
                    .putVariables(
                            "required-var",
                            io.littlehorse.sdk.common.proto.VariableValue.newBuilder()
                                    .setBool(true)
                                    .build())
                    .build());
        }

        waitEventualConsistency();
    }

    @Test
    public void shouldCountAllNodeRunsByWfSpecName() {
        Count count = client.countNodeRun(
                CountNodeRunRequest.newBuilder().setWfSpecName(WF_SPEC_NAME).build());

        // 3 versions * RUNS_PER_VERSION runs * nodesPerRun nodes
        int expectedTotal = 3 * RUNS_PER_VERSION * nodesPerRun;
        assertThat(count.getValue()).isEqualTo(expectedTotal);

        Count countMajor0 = client.countNodeRun(CountNodeRunRequest.newBuilder()
                .setWfSpecName(WF_SPEC_NAME)
                .setWfSpecMajorVersion(0)
                .build());

        // Major version 0 has 2 revisions (0/0 and 0/1), each with RUNS_PER_VERSION runs
        int expectedMajor0 = 2 * RUNS_PER_VERSION * nodesPerRun;
        assertThat(countMajor0.getValue()).isEqualTo(expectedMajor0);

        Count countMajor1 = client.countNodeRun(CountNodeRunRequest.newBuilder()
                .setWfSpecName(WF_SPEC_NAME)
                .setWfSpecMajorVersion(1)
                .build());

        // Major version 1 has 1 revision (1/0) with RUNS_PER_VERSION runs
        int expectedMajor1 = RUNS_PER_VERSION * nodesPerRun;
        assertThat(countMajor1.getValue()).isEqualTo(expectedMajor1);

        Count countRev0 = client.countNodeRun(CountNodeRunRequest.newBuilder()
                .setWfSpecName(WF_SPEC_NAME)
                .setWfSpecMajorVersion(0)
                .setWfSpecRevision(0)
                .build());

        int expectedRev0 = RUNS_PER_VERSION * nodesPerRun;
        assertThat(countRev0.getValue()).isEqualTo(expectedRev0);

        Count countRev1 = client.countNodeRun(CountNodeRunRequest.newBuilder()
                .setWfSpecName(WF_SPEC_NAME)
                .setWfSpecMajorVersion(0)
                .setWfSpecRevision(1)
                .build());

        int expectedRev1 = RUNS_PER_VERSION * nodesPerRun;
        assertThat(countRev1.getValue()).isEqualTo(expectedRev1);

        Count countMajor1Rev0 = client.countNodeRun(CountNodeRunRequest.newBuilder()
                .setWfSpecName(WF_SPEC_NAME)
                .setWfSpecMajorVersion(1)
                .setWfSpecRevision(0)
                .build());

        int expectedMajor1Rev0 = RUNS_PER_VERSION * nodesPerRun;
        assertThat(countMajor1Rev0.getValue()).isEqualTo(expectedMajor1Rev0);
    }

    private void waitEventualConsistency() {
        try {
            TimeUnit.MINUTES.sleep(1);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @LHTaskMethod("count-node-run-task")
    public String myTask() {
        return "Task executed";
    }
}
