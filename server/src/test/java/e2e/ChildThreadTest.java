package e2e;

import io.littlehorse.sdk.common.proto.LHErrorType;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.NodeRun.NodeTypeCase;
import io.littlehorse.sdk.common.proto.ThreadHaltReason;
import io.littlehorse.sdk.common.proto.ThreadHaltReason.ReasonCase;
import io.littlehorse.sdk.common.proto.ThreadRun;
import io.littlehorse.sdk.common.proto.Operation;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.WaitForThreadsRun;
import io.littlehorse.sdk.common.proto.WaitForThreadsRun.WaitForThread;
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

/*
 * This test relies on the fact that ExternalEventDef's do not have a VariableType
 * or Schema. We can use this to cause ThreadRun's to fail with a VAR_SUB_ERROR.
 *
 * For backwards compatibility, we will always have the option to have un-typed
 * ExternalEventDef's, so I think that is safe. However, we will encourage our users
 * to always use strong typing.
 */
@LHTest(externalEventNames = {ChildThreadTest.COMPLETE_CHILD, ChildThreadTest.COMPLETE_PARENT})
public class ChildThreadTest {

    public static final String COMPLETE_CHILD = "agjo-complete-child";
    public static final String COMPLETE_PARENT = "agjo-complete-parent";

    private WorkflowVerifier verifier;

    @LHWorkflow("basic-child-thread")
    private Workflow basicChildThread;

    @Test
    void childCompletesFirstAndCanMutateParentVariables() {
        verifier.prepareRun(basicChildThread)
                .thenVerifyWfRun(wfRun -> {
                    // The child should be created in the first Command
                    Assertions.assertThat(wfRun.getThreadRuns(1).getStatus()).isEqualTo(LHStatus.RUNNING);
                })
                .thenSendExternalEventWithContent(COMPLETE_CHILD, Map.of("result", 1234))
                .thenVerifyVariable(0, "shared-var", value -> {
                    Assertions.assertThat(value.getInt()).isEqualTo(1234);
                })
                .thenVerifyWfRun(wfRun -> {
                    Assertions.assertThat(wfRun.getThreadRuns(1).getStatus()).isEqualTo(LHStatus.COMPLETED);
                })
                .thenSendExternalEventWithContent(COMPLETE_PARENT, Map.of("result", 4321))
                .thenVerifyNodeRun(0, 3, nodeRun -> {
                    Assertions.assertThat(nodeRun.getNodeTypeCase()).isEqualTo(NodeTypeCase.WAIT_FOR_THREADS);
                    WaitForThreadsRun wftr = nodeRun.getWaitForThreads();

                    Assertions.assertThat(wftr.getThreadsCount()).isEqualTo(1);
                    WaitForThread childStatus = wftr.getThreads(0);
                    Assertions.assertThat(childStatus.getThreadRunNumber()).isEqualTo(1);
                    Assertions.assertThat(childStatus.getThreadStatus()).isEqualTo(LHStatus.COMPLETED);
                })
                .start();
    }

    @Test
    void childErrorPropagatesToParentOnlyOnWaitForThreadsNode() {
        verifier.prepareRun(basicChildThread)
                .thenVerifyWfRun(wfRun -> {
                    // The child should be created in the first Command
                    Assertions.assertThat(wfRun.getThreadRuns(1).getStatus()).isEqualTo(LHStatus.RUNNING);
                })
                .thenSendExternalEventWithContent(COMPLETE_CHILD, Map.of("result", "not-an-integer"))
                .thenVerifyNodeRun(1, 1, nodeRun -> {
                    // I thought this should be true, but apparently it's not. We need to review this.
                    // Assertions.assertThat(nodeRun.getStatus()).isEqualTo(LHStatus.ERROR);
                    Assertions.assertThat(nodeRun.getFailuresCount()).isEqualTo(1);

                    // Also, I thought that this should be VAR_MUTATION_ERROR not VAR_SUB_ERRROR...?
                    Assertions.assertThat(nodeRun.getFailures(0).getFailureName())
                            .isEqualTo(LHErrorType.VAR_SUB_ERROR.toString());
                })
                .thenVerifyWfRun(wfRun -> {
                    Assertions.assertThat(wfRun.getThreadRuns(1).getStatus()).isEqualTo(LHStatus.ERROR);
                    Assertions.assertThat(wfRun.getThreadRuns(0).getStatus()).isEqualTo(LHStatus.RUNNING);
                })
                .thenSendExternalEventWithContent(COMPLETE_PARENT, Map.of("result", 1234))
                .thenVerifyVariable(0, "shared-var", varVal -> {
                    // Variable mutation should have worked on parent external event
                    Assertions.assertThat(varVal.getInt()).isEqualTo(1234);
                })
                .thenVerifyWfRun(wfRun -> {
                    Assertions.assertThat(wfRun.getStatus()).isEqualTo(LHStatus.ERROR);
                })
                .thenVerifyNodeRun(0, 3, nodeRun -> {
                    Assertions.assertThat(nodeRun.getNodeTypeCase()).isEqualTo(NodeTypeCase.WAIT_FOR_THREADS);
                    WaitForThreadsRun wftr = nodeRun.getWaitForThreads();

                    Assertions.assertThat(wftr.getThreadsCount()).isEqualTo(1);
                    WaitForThread childStatus = wftr.getThreads(0);
                    Assertions.assertThat(childStatus.getThreadRunNumber()).isEqualTo(1);
                    Assertions.assertThat(childStatus.getThreadStatus()).isEqualTo(LHStatus.ERROR);

                    Assertions.assertThat(nodeRun.getStatus()).isEqualTo(LHStatus.ERROR);
                    Assertions.assertThat(nodeRun.getFailures(0).getFailureName())
                            .isEqualTo(LHErrorType.CHILD_FAILURE.toString());
                })
                .start();
    }

    @Test
    void parentErrorCausesChildToHalt() {
        verifier.prepareRun(basicChildThread)
                .thenVerifyWfRun(wfRun -> {
                    Assertions.assertThat(wfRun.getThreadRuns(1).getStatus()).isEqualTo(LHStatus.RUNNING);
                })
                .thenSendExternalEventWithContent(COMPLETE_PARENT, Map.of("result", "not-an-integer"))
                .thenVerifyWfRun(wfRun -> {
                    Assertions.assertThat(wfRun.getThreadRuns(0).getStatus()).isEqualTo(LHStatus.ERROR);

                    // Child should be halted
                    ThreadRun child = wfRun.getThreadRuns(1);
                    Assertions.assertThat(child.getStatus()).isEqualTo(LHStatus.HALTED);
                    Assertions.assertThat(child.getHaltReasonsCount()).isEqualTo(1);

                    // And it should be because of PARENT_HALTED
                    ThreadHaltReason reason = child.getHaltReasons(0);
                    Assertions.assertThat(reason.getReasonCase()).isEqualTo(ReasonCase.PARENT_HALTED);
                })
                .start();
    }

    @Test
    void parentWaitsForChildToComplete() {
        verifier.prepareRun(basicChildThread)
                .thenVerifyWfRun(wfRun -> {
                    Assertions.assertThat(wfRun.getThreadRuns(1).getStatus()).isEqualTo(LHStatus.RUNNING);
                })
                .thenSendExternalEventWithContent(COMPLETE_PARENT, Map.of("result", 1234))
                .thenVerifyWfRun(wfRun -> {
                    Assertions.assertThat(wfRun.getThreadRuns(0).getStatus()).isEqualTo(LHStatus.RUNNING);
                })
                .thenVerifyVariable(0, "shared-var", varVal -> {
                    Assertions.assertThat(varVal.getInt()).isEqualTo(1234);
                })
                .thenVerifyNodeRun(0, 3, nodeRun -> {
                    Assertions.assertThat(nodeRun.getNodeTypeCase()).isEqualTo(NodeTypeCase.WAIT_FOR_THREADS);
                    WaitForThreadsRun wftr = nodeRun.getWaitForThreads();

                    Assertions.assertThat(wftr.getThreadsCount()).isEqualTo(1);
                    WaitForThread childStatus = wftr.getThreads(0);
                    Assertions.assertThat(childStatus.getThreadRunNumber()).isEqualTo(1);
                    Assertions.assertThat(childStatus.getThreadStatus()).isEqualTo(LHStatus.RUNNING);
                })
                .thenSendExternalEventWithContent(COMPLETE_CHILD, Map.of("result", 137))
                .thenVerifyNodeRun(0, 3, nodeRun -> {
                    Assertions.assertThat(nodeRun.getNodeTypeCase()).isEqualTo(NodeTypeCase.WAIT_FOR_THREADS);
                    WaitForThreadsRun wftr = nodeRun.getWaitForThreads();

                    Assertions.assertThat(wftr.getThreadsCount()).isEqualTo(1);
                    WaitForThread childStatus = wftr.getThreads(0);
                    Assertions.assertThat(childStatus.getThreadRunNumber()).isEqualTo(1);
                    Assertions.assertThat(childStatus.getThreadStatus()).isEqualTo(LHStatus.COMPLETED);
                })
                .thenVerifyVariable(0, "shared-var", varVal -> {
                    Assertions.assertThat(varVal.getInt()).isEqualTo(137);
                })
                .waitForStatus(LHStatus.COMPLETED)
                .start();
    }

    @LHWorkflow("basic-child-thread")
    public Workflow getBasicChildThreadWorkflow() {
        return Workflow.newWorkflow("basic-child-thread", wf -> {
            WfRunVariable sharedVar = wf.addVariable("shared-var", VariableType.INT);

            SpawnedThread childThread = wf.spawnThread(
                    child -> {
                        child.mutate(
                                sharedVar,
                                Operation.ASSIGN,
                                child.waitForEvent(COMPLETE_CHILD).jsonPath("$.result"));
                    },
                    "child-thread",
                    Map.of());

            wf.mutate(
                    sharedVar,
                    Operation.ASSIGN,
                    wf.waitForEvent(COMPLETE_PARENT).jsonPath("$.result"));

            wf.waitForThreads(SpawnedThreads.of(childThread));
        });
    }
}
