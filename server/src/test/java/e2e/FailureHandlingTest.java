package e2e;

import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.wfsdk.NodeOutput;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.LHWorkflow;
import io.littlehorse.test.WorkflowVerifier;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

@LHTest
public class FailureHandlingTest {

    private WorkflowVerifier workflowVerifier;

    @LHWorkflow("handle-error-wf")
    private Workflow handleErrorWf;

    @Test
    public void shouldHandleAnyError() {
        workflowVerifier
                .prepareRun(handleErrorWf)
                .waitForStatus(LHStatus.COMPLETED)
                .waitForNodeRunStatus(0, 1, LHStatus.ERROR)
                .thenVerifyNodeRun(1, 1, nodeRun -> Assertions.assertThat(nodeRun.getStatus())
                        .isEqualTo(LHStatus.COMPLETED))
                .start();
    }

    @LHWorkflow("handle-error-wf")
    public Workflow handleErrorWorkflow() {
        return new WorkflowImpl("example-exception-handler", thread -> {
            NodeOutput node = thread.execute("fail");
            thread.handleError(node, handler -> {
                handler.execute("my-handler");
            });
            thread.execute("my-task");
        });
    }

    @LHTaskMethod("fail")
    public String fail() {
        throw new RuntimeException("something went wrong!");
    }

    @LHTaskMethod("my-handler")
    public String handler() {
        return "Exception handled";
    }

    @LHTaskMethod("my-task")
    public String task() {
        return "Task executed";
    }
}
