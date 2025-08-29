package e2e;

import io.littlehorse.sdk.common.proto.Comparator;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.VariableMutationType;
import io.littlehorse.sdk.common.util.Arg;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.LHWorkflow;
import io.littlehorse.test.WorkflowVerifier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@LHTest(externalEventNames = WaitForConditionTest.EVENT_NAME)
public class WaitForConditionTest {

    public static final String EVENT_NAME = "wait-for-condition-decrement-counter";
    private WorkflowVerifier verifier;

    @LHWorkflow("wait-for-condition")
    public Workflow waitForConditionWorkflow;

    @Test
    void waitForConditionCompletesWhenChildMutatesParentVar() {
        verifier.prepareRun(waitForConditionWorkflow)
                .thenVerifyWfRun(wfRun -> {
                    Assertions.assertEquals(LHStatus.RUNNING, wfRun.getStatus());
                })
                .thenSendExternalEventWithContent(EVENT_NAME, null)
                .thenVerifyWfRun(wfRun -> {
                    Assertions.assertEquals(LHStatus.RUNNING, wfRun.getStatus());
                })
                .thenSendExternalEventWithContent(EVENT_NAME, null)
                .waitForStatus(LHStatus.COMPLETED)
                .start();
    }

    @Test
    void waitForConditionShouldPassIfAlreadymet() {
        verifier.prepareRun(waitForConditionWorkflow, Arg.of("counter", 0))
                .waitForStatus(LHStatus.COMPLETED)
                .start();
    }

    @LHWorkflow("wait-for-condition")
    public Workflow getWaitForConditionWorkflow() {
        return Workflow.newWorkflow("wait-for-condition", wf -> {
            WfRunVariable counter = wf.addVariable("counter", 2);

            wf.waitForCondition(wf.condition(counter, Comparator.EQUALS, 0));

            // Interrupt handler which mutates the parent variable
            wf.registerInterruptHandler(EVENT_NAME, handler -> {
                handler.mutate(counter, VariableMutationType.SUBTRACT, 1);
            });
        });
    }
}
