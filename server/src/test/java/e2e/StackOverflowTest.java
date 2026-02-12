package e2e;

import io.littlehorse.common.LHConstants;
import io.littlehorse.sdk.common.proto.Operation;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.LHWorkflow;
import io.littlehorse.test.WorkflowVerifier;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

@LHTest
public class StackOverflowTest {

    private WorkflowVerifier verifier;

    @LHWorkflow("infinite-tight-loop")
    private Workflow infiniteTightLoop;

    @LHWorkflow("infinite-tight-loop")
    public Workflow getTightLoop() {
        return Workflow.newWorkflow("infinite-tight-loop", wf -> {
            wf.doWhile(wf.condition(true, Operation.EQUALS, true), loop -> {});
        });
    }

    @Test
    void shouldGoToErrorAfter25iterations() {
        verifier.prepareRun(infiniteTightLoop)
                .waitForStatus(LHStatus.ERROR)
                .thenVerifyWfRun(wfRun -> {
                    Assertions.assertThat(wfRun.getThreadRuns(0)
                            .getErrorMessage()
                            .toLowerCase()
                            .contains("stack overflow"));
                })
                .thenVerifyWfRun(wfRun -> {
                    Assertions.assertThat(wfRun.getThreadRuns(0).getCurrentNodePosition())
                            .isEqualTo(LHConstants.MAX_STACK_FRAMES_PER_COMMAND);
                })
                .start();
    }
}
