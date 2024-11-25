package e2e;

import io.littlehorse.sdk.common.proto.Failure;
import io.littlehorse.sdk.common.proto.LHErrorType;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.util.Arg;
import io.littlehorse.sdk.wfsdk.LHExpression;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.LHWorkflow;
import io.littlehorse.test.WorkflowVerifier;
import java.util.Map;
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
                    Assertions.assertEquals("hello-suffix", variable.getStr());
                })
                .start();
    }

    @Test
    void shouldAddIntAndIgnoreLHSWhenAssigning() {
        verifier.prepareRun(expressionWf, Arg.of("int-to-add", 1), Arg.of("int-to-add-result", 137))
                .waitForStatus(LHStatus.COMPLETED)
                .thenVerifyVariable(0, "int-to-add-result", variable -> {
                    Assertions.assertEquals(3, variable.getInt());
                })
                .start();
    }

    @Test
    void shouldNotChangeVariableUsedInExpression() {
        verifier.prepareRun(expressionWf, Arg.of("int-to-add", 1))
                .waitForStatus(LHStatus.COMPLETED)
                .thenVerifyVariable(0, "int-to-add", variable -> {
                    Assertions.assertEquals(1, variable.getInt());
                })
                .start();
    }

    @Test
    void shouldAddIntAndAssignToNullVariable() {
        verifier.prepareRun(expressionWf, Arg.of("int-to-add", 1))
                .waitForStatus(LHStatus.COMPLETED)
                .thenVerifyVariable(0, "int-to-add-result", variable -> {
                    Assertions.assertEquals(3, variable.getInt());
                })
                .start();
    }

    @Test
    void dividingByZeroThrowsVarSubError() {
        verifier.prepareRun(expressionWf, Arg.of("thing-to-divide-by-zero", 1))
                .waitForStatus(LHStatus.ERROR)
                .thenVerifyLastNodeRun(0, nodeRun -> {
                    // TODO: Issue #1083 will allow us to uncomment this assertion.
                    // Assertions.assertEquals(LHStatus.ERROR, nodeRun.getStatus());
                    Failure mathTest = nodeRun.getFailures(0);
                    Assertions.assertEquals(mathTest.getFailureName(), LHErrorType.VAR_SUB_ERROR.toString());
                    Assertions.assertTrue(mathTest.getMessage().toLowerCase().contains("divide by zero"));
                })
                .start();
    }

    @Test
    void dividingTwoIntsShouldUseIntegerDivision() {
        verifier.prepareRun(expressionWf, Arg.of("dividend-tests", Map.of("lhs", 5, "rhs", 2)))
                .waitForStatus(LHStatus.COMPLETED)
                .thenVerifyVariable(0, "division-result", variable -> {
                    Assertions.assertEquals(2.0, variable.getDouble());
                })
                .start();
    }

    @Test
    void dividingDoubleByIntShouldUseFloatingDivision() {
        verifier.prepareRun(expressionWf, Arg.of("dividend-tests", Map.of("lhs", 5.0, "rhs", 2)))
                .waitForStatus(LHStatus.COMPLETED)
                .thenVerifyVariable(0, "division-result", variable -> {
                    Assertions.assertEquals(2.5, variable.getDouble());
                })
                .start();
    }

    @Test
    void dividingIntByDoubleShouldUseFloatingDivision() {
        verifier.prepareRun(expressionWf, Arg.of("dividend-tests", Map.of("lhs", 5, "rhs", 2.5)))
                .waitForStatus(LHStatus.COMPLETED)
                .thenVerifyVariable(0, "division-result", variable -> {
                    Assertions.assertEquals(2.5, variable.getDouble());
                })
                .start();
    }

    @Test
    void dividingTwoDoublesShouldUseFloatingDivision() {
        verifier.prepareRun(expressionWf, Arg.of("dividend-tests", Map.of("lhs", 5.0, "rhs", 2.5)))
                .waitForStatus(LHStatus.COMPLETED)
                .thenVerifyVariable(0, "division-result", variable -> {
                    Assertions.assertEquals(2.5, variable.getDouble());
                })
                .start();
    }

    @Test
    void shouldCastDoubleToIntWhenAssigningToInt() {
        verifier.prepareRun(expressionWf, Arg.of("dividend-tests", Map.of("lhs", 5.0, "rhs", 2.5)))
                .waitForStatus(LHStatus.COMPLETED)
                .thenVerifyVariable(0, "division-result-int", variable -> {
                    Assertions.assertEquals(2, variable.getInt());
                })
                .start();
    }

    @Test
    void shouldGetVarSubErrorMultiplyingNull() {
        verifier.prepareRun(
                        expressionWf, Arg.of("quantity", 5), Arg.of("price", 2.5), Arg.of("discount-percentage", null))
                .waitForStatus(LHStatus.ERROR)
                .thenVerifyLastNodeRun(0, nodeRun -> {
                    // TODO: Issue #1083 will allow us to uncomment this assertion.
                    // Assertions.assertEquals(LHStatus.ERROR, nodeRun.getStatus());
                    Failure mathTest = nodeRun.getFailures(0);
                    Assertions.assertEquals(mathTest.getFailureName(), LHErrorType.VAR_SUB_ERROR.toString());
                    Assertions.assertTrue(mathTest.getMessage().toLowerCase().contains("null"));
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
            // EXTEND a String test
            var myStr = wf.declareStr("my-str");
            wf.doIf(myStr.isNotEqualTo(null), then -> {
                myStr.assignTo(myStr.extend("-suffix"));
            });

            // Add an int and composite expressions
            var intToAdd = wf.declareInt("int-to-add");
            var intToAddResult = wf.declareInt("int-to-add-result");
            wf.doIf(intToAdd.isNotEqualTo(null), then -> {
                // Tests compound expressions
                intToAddResult.assignTo(wf.execute("expr-add-one", intToAdd.add(1)));
            });

            // Division By Zero test
            var thingToDivideByZero = wf.declareInt("thing-to-divide-by-zero");
            var divideByZeroResult = wf.declareInt("divide-by-zero-result");
            wf.doIf(thingToDivideByZero.isNotEqualTo(null), then -> {
                divideByZeroResult.assignTo(thingToDivideByZero.divide(0));
            });

            // Test precision of arithmetic. Make use of the fact that we don't have
            // strong typing on json objects so that we can use a jsonpath to arbitrarily
            // set input values.
            var divisionTestJson = wf.declareJsonObj("dividend-tests");
            var divisionResult = wf.declareDouble("division-result");
            var divisionResultInt = wf.declareDouble("division-result-int");
            wf.doIf(divisionTestJson.isNotEqualTo(null), then -> {
                divisionResult.assignTo(divisionTestJson.jsonPath("$.lhs").divide(divisionTestJson.jsonPath("$.rhs")));
                divisionResultInt.assignTo(
                        divisionTestJson.jsonPath("$.lhs").divide(divisionTestJson.jsonPath("$.rhs")));
            });

            // This test uses a complex expression where the things we are computing over
            // have the double precision. We want to make sure that the computation is executed
            // with double precision whether we assign the result to an int or a double.
            var quantity = wf.declareInt("quantity");
            var price = wf.declareDouble("price");
            var discountPercentage = wf.declareInt("discount-percentage");
            var totalPriceInt = wf.declareInt("total-price-int");
            var totalPriceDouble = wf.declareDouble("total-price-double");
            wf.doIf(quantity.isNotEqualTo(null), then -> {
                LHExpression pedro = quantity.multiply(
                        price.multiply(wf.subtract(100, discountPercentage).multiply(100)));
                totalPriceInt.assignTo(pedro);
                totalPriceDouble.assignTo(pedro);
            });
        });
    }

    @LHTaskMethod("expr-add-one")
    public int addOne(int input) {
        return input + 1;
    }
}
