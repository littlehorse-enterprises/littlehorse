package e2e;

import io.littlehorse.sdk.common.proto.Failure;
import io.littlehorse.sdk.common.proto.LHErrorType;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.util.Arg;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.LHWorkflow;
import io.littlehorse.test.WorkflowVerifier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@LHTest
public class ExpressionTest {

    @LHWorkflow("basic-expression-manipulation")
    private Workflow expressionWf;

    private WorkflowVerifier verifier;

    @Test
    public void shouldExtendString() {
        verifier.prepareRun(expressionWf, Arg.of("my-str", "hello"))
                .waitForStatus(LHStatus.COMPLETED)
                .thenVerifyVariable(0, "my-str", variable -> {
                    Assertions.assertEquals(variable.getStr(), "hello-suffix");
                })
                .start();
    }

    @Test
    void shouldAddIntAndIgnoreLHSWhenAssigning() {
        verifier.prepareRun(expressionWf, Arg.of("int-to-add", 1), Arg.of("int-to-add-result", 137))
                .waitForStatus(LHStatus.COMPLETED)
                .thenVerifyVariable(0, "int-to-add-result", variable -> {
                    Assertions.assertEquals(variable.getInt(), 2);
                })
                .start();
    }

    @Test
    void shouldAddIntAndAssignToNullVariable() {
        verifier.prepareRun(expressionWf, Arg.of("int-to-add", 1))
                .waitForStatus(LHStatus.COMPLETED)
                .thenVerifyVariable(0, "int-to-add-result", variable -> {
                    Assertions.assertEquals(variable.getInt(), 2);
                })
                .start();
    }

    @Test
    void dividingByZeroThrowsVarSubError() {
        verifier.prepareRun(expressionWf, Arg.of("thing-to-divide-by-zero", 1))
                .waitForStatus(LHStatus.ERROR)
                .thenVerifyLastNodeRun(0, nodeRun -> {
                    Assertions.assertEquals(nodeRun.getStatus(), LHStatus.ERROR);
                    Failure mathTest = nodeRun.getFailures(0);
                    Assertions.assertEquals(mathTest.getFailureName(), LHErrorType.VAR_SUB_ERROR.toString());
                    Assertions.assertTrue(mathTest.getMessage().toLowerCase().contains("divide by zero"));
                })
                .start();
    }

    /*
     * Each of the tests in here *could* be their own Workflow; however, registering a
     * workflow takes ~200ms in our testing. Each of these test cases takes about 15ms
     * to run because it doesn't involve executing a TaskRun. Therefore, even though
     * it is less clean to have everything in one workflow, we can still achieve many
     * test cases while adding less than one second to our pipeline.
     *
     * Additionally, we have an individual @Test case for each of the logical test cases;
     * the only difference is that they all share the same WfSpec.
     */
    @LHWorkflow("basic-expression-manipulation")
    public Workflow getExpression() {
        return new WorkflowImpl("test-expression", wf -> {
            WfRunVariable myStr = wf.declareStr("my-str");
            wf.doIf(myStr.isNotEqualTo(null), then -> {
                myStr.assignTo(myStr.extend("-suffix"));
            });

            WfRunVariable intToAdd = wf.declareInt("int-to-add");
            WfRunVariable intToAddResult = wf.declareInt("int-to-add-result");
            wf.doIf(intToAdd.isNotEqualTo(null), then -> {
                intToAddResult.assignTo(wf.execute("add-one", intToAdd));
            });

            WfRunVariable thingToDivideByZero = wf.declareInt("thing-to-divide-by-zero");
            WfRunVariable divideByZeroResult = wf.declareInt("divide-by-zero-result");
            wf.doIf(thingToDivideByZero.isNotEqualTo(null), then -> {
                divideByZeroResult.assignTo(thingToDivideByZero.divide(0));
            });

        });
    }

    @LHTaskMethod("expr-echo-string")
    public String echoString(String input) {
        return input;
    }
}
