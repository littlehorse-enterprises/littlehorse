package e2e;

import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.util.Arg;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.LHWorkflow;
import io.littlehorse.test.WorkflowVerifier;
import org.junit.jupiter.api.Test;

@LHTest
public class TaskReturnNullTest {

    private WorkflowVerifier verifier;

    @LHWorkflow("task-null-return-wf")
    private Workflow wf;

    @LHWorkflow("task-null-return-wf")
    public Workflow getWorkflow() {
        return Workflow.newWorkflow("task-null-return-wf", wf -> {
            WfRunVariable in = wf.declareStr("in").required();
            wf.execute("maybe-return-int", in);
            wf.complete();
        });
    }

    @Test
    public void taskReturningNullIsAccepted() {
        verifier.prepareRun(getWorkflow(), Arg.of("in", "whatever"))
                .waitForStatus(LHStatus.COMPLETED)
                .start();
    }

    @LHTaskMethod("maybe-return-int")
    public Integer maybeReturnInt(String input) {
        return null;
    }
}
