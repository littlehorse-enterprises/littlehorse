package e2e;

import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.Operation;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.wfsdk.NodeOutput;
import io.littlehorse.sdk.wfsdk.SpawnedThread;
import io.littlehorse.sdk.wfsdk.SpawnedThreads;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.LHWorkflow;
import io.littlehorse.test.WorkflowVerifier;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

@LHTest
public class OutOfScopeVarAccessTest {

    private WorkflowVerifier verifier;

    // Need to put it here so that we can access it in an illegal manner
    private WfRunVariable variableOnlyAccessibleInChild;

    @LHWorkflow("access-out-of-scope-var")
    private Workflow accessOutOfScopeVarWf;

    @Test
    void shouldThrowVarSubErrorOnIllegalAccess() {
        verifier.prepareRun(accessOutOfScopeVarWf)
                // this WfRun should be terminated on the first RPC since there are no
                // TaskRun's, ExternalEvents, or User Tasks. Everything is synchronous.
                //
                // Therefore, we should NOT do a waitForStatus().
                .thenVerifyWfRun(wfRun -> {
                    Assertions.assertThat(wfRun.getStatus()).isEqualTo(LHStatus.ERROR);
                    Assertions.assertThat(wfRun.getThreadRuns(1).getStatus()).isEqualTo(LHStatus.EXCEPTION);
                    Assertions.assertThat(wfRun.getThreadRuns(2).getStatus()).isEqualTo(LHStatus.ERROR);
                })
                .start();
    }

    @LHWorkflow("access-out-of-scope-var")
    public Workflow getAccessOutOfScopeVarWf() {
        return Workflow.newWorkflow("access-out-of-scope-var", wf -> {
            SpawnedThread spawnedThread = wf.spawnThread(
                    child -> {
                        variableOnlyAccessibleInChild = child.addVariable("out-of-scope-var", VariableType.BOOL);
                        child.fail("some-failure", "some failure message");
                    },
                    "child-thread",
                    Map.of());

            // In order to "trick" the SDK into creating a WfSpec that accesses an
            // out-of-scope variable, we have to use a failure handler on a waitForThreads
            // node.
            NodeOutput nodeThatWillFail = wf.waitForThreads(SpawnedThreads.of(spawnedThread));

            wf.handleException(nodeThatWillFail, handler -> {
                // We once had an NPE in which accessing a variable from the child in this thread
                // was permitted by the SDK but the server threw an orzdash.
                handler.mutate(variableOnlyAccessibleInChild, Operation.ASSIGN, true);
            });
        });
    }
}
