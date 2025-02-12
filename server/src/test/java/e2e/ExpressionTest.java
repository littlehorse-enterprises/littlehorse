package e2e;

import io.littlehorse.sdk.common.LHLibUtil;
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
                    Assertions.assertEquals(LHStatus.ERROR, nodeRun.getStatus());
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
        verifier.prepareRun(expressionWf, Arg.of("dividend-tests", Map.of("lhs", 5, "rhs", 2.0)))
                .waitForStatus(LHStatus.COMPLETED)
                .thenVerifyVariable(0, "division-result", variable -> {
                    Assertions.assertEquals(2.5, variable.getDouble());
                })
                .start();
    }

    @Test
    void dividingTwoDoublesShouldUseFloatingDivision() {
        verifier.prepareRun(expressionWf, Arg.of("dividend-tests", Map.of("lhs", 5.0, "rhs", 2.0)))
                .waitForStatus(LHStatus.COMPLETED)
                .thenVerifyVariable(0, "division-result", variable -> {
                    Assertions.assertEquals(2.5, variable.getDouble());
                })
                .start();
    }

    @Test
    void shouldCastDoubleToIntWhenAssigningToInt() {
        verifier.prepareRun(expressionWf, Arg.of("dividend-tests", Map.of("lhs", 5.0, "rhs", 2.0)))
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
                    Assertions.assertEquals(LHStatus.ERROR, nodeRun.getStatus());
                    Failure mathTest = nodeRun.getFailures(0);
                    Assertions.assertEquals(mathTest.getFailureName(), LHErrorType.VAR_SUB_ERROR.toString());
                    Assertions.assertTrue(mathTest.getMessage().toLowerCase().contains("value_not_set")
                            || mathTest.getMessage().toLowerCase().contains("null"));
                })
                .start();
    }

    @Test
    void shouldCalculateDiscountProperly() {
        verifier.prepareRun(
                        expressionWf, Arg.of("quantity", 10), Arg.of("price", 100.3), Arg.of("discount-percentage", 10))
                .waitForStatus(LHStatus.COMPLETED)
                .thenVerifyVariable(0, "total-price-double", variable -> {
                    Assertions.assertEquals(902.7, variable.getDouble());
                })
                .start();
    }

    @Test
    void shouldStillUseDoublePrecisionWhenCalculatingBeforeAssigningToAnInt() {
        verifier.prepareRun(
                        expressionWf, Arg.of("quantity", 10), Arg.of("price", 100.3), Arg.of("discount-percentage", 10))
                .waitForStatus(LHStatus.COMPLETED)
                .thenVerifyVariable(0, "total-price-int", variable -> {
                    Assertions.assertEquals(902, variable.getInt());
                })
                .start();
    }

    @Test
    void shouldOverwriteJsonValueWhenMutating() {
        verifier.prepareRun(expressionWf, Arg.of("json", Map.of("foo", "baz")))
                .waitForStatus(LHStatus.COMPLETED)
                .thenVerifyVariable(0, "json", variable -> {
                    String jsonStr = variable.getJsonObj();
                    @SuppressWarnings("unchecked")
                    Map<String, Object> jsonMap = LHLibUtil.LH_GSON.fromJson(jsonStr, Map.class);
                    Assertions.assertEquals("bar", jsonMap.get("foo"));
                })
                .start();
    }

    @Test
    void shouldAddKeyIfNotExists() {
        verifier.prepareRun(expressionWf, Arg.of("json", Map.of()))
                .waitForStatus(LHStatus.COMPLETED)
                .thenVerifyVariable(0, "json", variable -> {
                    String jsonStr = variable.getJsonObj();
                    @SuppressWarnings("unchecked")
                    Map<String, Object> jsonMap = LHLibUtil.LH_GSON.fromJson(jsonStr, Map.class);
                    Assertions.assertEquals("bar", jsonMap.get("foo"));
                })
                .start();
    }

    @Test
    void shouldThrowOnSecondLayerNullNestedField() {
        verifier.prepareRun(expressionWf, Arg.of("nested-json", Map.of()))
                .waitForStatus(LHStatus.ERROR)
                .thenVerifyLastNodeRun(0, nodeRun -> {
                    System.out.println(nodeRun.getFailures(0).getMessage());
                    Assertions.assertTrue(
                            nodeRun.getFailures(0).getMessage().toLowerCase().contains("missing property"));
                })
                .start();
    }

    @Test
    void shouldSetNullNestedFieldWhenFirstLayerProvided() {
        verifier.prepareRun(expressionWf, Arg.of("nested-json", Map.of("foo", Map.of())))
                .waitForStatus(LHStatus.COMPLETED)
                .thenVerifyVariable(0, "nested-json", variable -> {
                    String jsonStr = variable.getJsonObj();
                    @SuppressWarnings("unchecked")
                    Map<String, Object> jsonMap = LHLibUtil.LH_GSON.fromJson(jsonStr, Map.class);
                    @SuppressWarnings("unchecked")
                    Map<String, String> fooMap = (Map<String, String>) jsonMap.get("foo");
                    Assertions.assertEquals("baz", fooMap.get("bar"));
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
                myStr.assign(myStr.extend("-suffix"));
            });

            // Add an int and composite expressions
            var intToAdd = wf.declareInt("int-to-add");
            var intToAddResult = wf.declareInt("int-to-add-result");
            wf.doIf(intToAdd.isNotEqualTo(null), then -> {
                // Tests compound expressions
                intToAddResult.assign(wf.execute("expr-add-one", intToAdd.add(1)));
            });

            // Division By Zero test
            var thingToDivideByZero = wf.declareInt("thing-to-divide-by-zero");
            var divideByZeroResult = wf.declareInt("divide-by-zero-result");
            wf.doIf(thingToDivideByZero.isNotEqualTo(null), then -> {
                divideByZeroResult.assign(thingToDivideByZero.divide(0));
            });

            // Test precision of arithmetic. Make use of the fact that we don't have
            // strong typing on json objects so that we can use a jsonpath to arbitrarily
            // set input values.
            var divisionTestJson = wf.declareJsonObj("dividend-tests");
            var divisionResult = wf.declareDouble("division-result");
            var divisionResultInt = wf.declareInt("division-result-int");
            wf.doIf(divisionTestJson.isNotEqualTo(null), then -> {
                LHExpression foobar = divisionTestJson.jsonPath("$.lhs").divide(divisionTestJson.jsonPath("$.rhs"));
                divisionResult.assign(foobar);
                divisionResultInt.assign(foobar);
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
                // TotalPrice = Quantity * Price * (1 - DiscountPercentage / 100)
                LHExpression pedro =
                        quantity.multiply(price).multiply(wf.subtract(1.0, discountPercentage.divide(100.0)));
                totalPriceInt.assign(pedro);
                totalPriceDouble.assign(pedro);
            });

            // Test mutating sub-fields of a json object
            var json = wf.declareJsonObj("json");
            wf.doIf(json.isNotEqualTo(null), then -> {
                json.jsonPath("$.foo").assign("bar");
            });

            // Test mutating doubly-nested fields of a Json Object
            var nestedJson = wf.declareJsonObj("nested-json");
            wf.doIf(nestedJson.isNotEqualTo(null), then -> {
                nestedJson.jsonPath("$.foo.bar").assign("baz");
            });
        });
    }

    @LHTaskMethod("expr-add-one")
    public int addOne(int input) {
        return input + 1;
    }
}
