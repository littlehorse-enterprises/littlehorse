package e2e;

import static org.assertj.core.api.Assertions.*;

import e2e.Struct.Car;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.util.Arg;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.LHWorkflow;
import io.littlehorse.test.WithStructDefs;
import io.littlehorse.test.WorkflowVerifier;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@LHTest
@WithStructDefs({Car.class})
public class ConditionalsTest {
    private LittleHorseBlockingStub client;

    @LHWorkflow("test-conditionals-equals-workflow")
    private Workflow workflowEquals;

    @LHWorkflow("test-conditionals-struct-equals-workflow")
    private Workflow workflowStructEquals;

    @LHWorkflow("test-conditionals-not-equals-workflow")
    private Workflow workflowNotEquals;

    @LHWorkflow("test-conditionals-less-than-workflow")
    private Workflow workflowLessThan;

    @LHWorkflow("test-conditionals-less-than-equals-workflow")
    private Workflow workflowLessThanEquals;

    @LHWorkflow("test-conditionals-greater-than-workflow")
    private Workflow workflowGreaterThan;

    @LHWorkflow("test-conditionals-greater-than-equals-workflow")
    private Workflow workflowGreaterThanEquals;

    @LHWorkflow("test-conditionals-is-in-workflow")
    private Workflow workflowIsIn;

    @LHWorkflow("test-conditionals-not-in-workflow")
    private Workflow workflowNotIn;

    @LHWorkflow("test-conditionals-does-contain-workflow")
    private Workflow workflowDoesContain;

    @LHWorkflow("test-conditionals-does-not-contain-workflow")
    private Workflow workflowDoesNotContain;

    @LHWorkflow("test-conditionals-bool-var-do-if")
    private Workflow workflowBoolVarDoIf;

    @LHWorkflow("test-conditionals-bool-var-do-if-else")
    private Workflow workflowBoolVarDoIfElse;

    @LHWorkflow("test-nested-if")
    private Workflow workflowNestedIf;

    @LHWorkflow("test-nested-if-else")
    private Workflow workflowNestedIfElse;

    private WorkflowVerifier workflowVerifier;

    @Nested
    @WithStructDefs({Car.class})
    class Equals {

        @ParameterizedTest
        @MethodSource("provideEqualsWorkflowSuccessArguments")
        void shouldCompleteEqualsWorkflowWithConditionals(Map<?, ?> inputObject, boolean expectedOutput) {
            workflowVerifier
                    .prepareRun(workflowEquals, Arg.of("input", inputObject))
                    .waitForStatus(LHStatus.COMPLETED)
                    .thenVerifyTaskRunResult(0, 1, variableValue -> assertThat(variableValue.getBool())
                            .isEqualTo(true))
                    .thenVerifyTaskRunResult(0, 3, variableValue -> assertThat(variableValue.getBool())
                            .isEqualTo(expectedOutput))
                    .start();
        }

        @ParameterizedTest
        @MethodSource("provideEqualsWorkflowInvalidArguments")
        void shouldFailEqualsWorkflowWithInvalidInput(Object lhs, Object rhs) {
            InputObj inputObj = new InputObj(lhs, rhs);
            workflowVerifier
                    .prepareRun(workflowEquals, Arg.of("input", inputObj))
                    .waitForStatus(LHStatus.ERROR)
                    .start();
        }

        private static Stream<Arguments> provideEqualsWorkflowInvalidArguments() {
            return Stream.of(
                    Arguments.of(Map.of("a", 1), Map.of("a", 1)),
                    Arguments.of(Arrays.asList(0), Arrays.asList(0)),
                    Arguments.of(false, "false"),
                    Arguments.of(1, 1.0),
                    Arguments.of(1, "one"),
                    Arguments.of(2, "2"),
                    Arguments.of(2, Map.of("a", 1)));
        }

        private static Stream<Arguments> provideEqualsWorkflowSuccessArguments() {
            return Stream.of(
                    Arguments.of(Map.of("lhs", 2, "rhs", 1), false),
                    Arguments.of(Map.of("lhs", 2, "rhs", 2), true),
                    Arguments.of(Map.of("lhs", "hi", "rhs", "hi"), true),
                    Arguments.of(Map.of("lhs", 1.0, "rhs", 2.0), false));
        }

        @ParameterizedTest
        @MethodSource("provideStructEqualsWorkflowSuccessArguments")
        void shouldCompleteStructEqualsWorkflowWithConditionals(Car car1, Car car2, boolean expectedOutput) {
            workflowVerifier
                    .prepareRun(workflowStructEquals, Arg.of("struct-a", car1), Arg.of("struct-b", car2))
                    .waitForStatus(LHStatus.COMPLETED)
                    .thenVerifyTaskRunResult(0, 1, variableValue -> assertThat(variableValue.getBool())
                            .isEqualTo(true))
                    .thenVerifyTaskRunResult(0, 3, variableValue -> assertThat(variableValue.getBool())
                            .isEqualTo(expectedOutput))
                    .start();
        }

        private static Stream<Arguments> provideStructEqualsWorkflowSuccessArguments() {
            return Stream.of(
                    Arguments.of(new Car("Honda", "Civic", 20000000), new Car("Honda", "Civic", 20000000), true),
                    Arguments.of(new Car("Tesla", "Model X", 15000), new Car("Honda", "Civic", 20000000), false));
        }
    }

    @Nested
    class NotEquals {
        @ParameterizedTest
        @MethodSource("provideSuccessArguments")
        void shouldCompleteNotEqualsWorkflowWithConditionals(InputObj inputObject, boolean expectedOutput) {
            workflowVerifier
                    .prepareRun(workflowNotEquals, Arg.of("input", inputObject))
                    .waitForStatus(LHStatus.COMPLETED)
                    .thenVerifyTaskRunResult(0, 1, variableValue -> assertThat(variableValue.getBool())
                            .isEqualTo(true))
                    .thenVerifyTaskRunResult(0, 3, variableValue -> assertThat(variableValue.getBool())
                            .isEqualTo(expectedOutput))
                    .start();
        }

        @ParameterizedTest
        @MethodSource("provideInvalidArguments")
        void shouldFailEqualsWorkflowWithInvalidInput(InputObj inputObj) {
            workflowVerifier
                    .prepareRun(workflowNotEquals, Arg.of("input", inputObj))
                    .waitForStatus(LHStatus.ERROR)
                    .start();
        }

        private static Stream<Arguments> provideSuccessArguments() {
            return Stream.of(
                    Arguments.of(new ConditionalsTest.InputObj(1, 2), true),
                    Arguments.of(new ConditionalsTest.InputObj(1, 1), false),
                    Arguments.of(new ConditionalsTest.InputObj("hi", "hi"), false),
                    Arguments.of(new ConditionalsTest.InputObj(1.0, 1.0), false));
        }

        private static Stream<Arguments> provideInvalidArguments() {
            return Stream.of(
                    Arguments.of(new ConditionalsTest.InputObj(Map.of("a", 1), Map.of("a", 1))),
                    Arguments.of(new ConditionalsTest.InputObj(Arrays.asList(0), Arrays.asList(0))),
                    Arguments.of(new ConditionalsTest.InputObj(false, "false")),
                    Arguments.of(new ConditionalsTest.InputObj(1, 1.0)),
                    Arguments.of(new ConditionalsTest.InputObj(1, 1.0)),
                    Arguments.of(new ConditionalsTest.InputObj(1, "one")),
                    Arguments.of(new ConditionalsTest.InputObj(2, "2")),
                    Arguments.of(new ConditionalsTest.InputObj(2, Map.of("a", 1))));
        }
    }

    @Nested
    class LessThan {
        @ParameterizedTest
        @MethodSource("provideSuccessArguments")
        void shouldCompleteLessThanWorkflowWithConditionals(InputObj inputObject, boolean expectedOutput) {
            workflowVerifier
                    .prepareRun(workflowLessThan, Arg.of("input", inputObject))
                    .waitForStatus(LHStatus.COMPLETED)
                    .thenVerifyTaskRunResult(0, 1, variableValue -> assertThat(variableValue.getBool())
                            .isEqualTo(true))
                    .thenVerifyTaskRunResult(0, 3, variableValue -> assertThat(variableValue.getBool())
                            .isEqualTo(expectedOutput))
                    .start();
        }

        private static Stream<Arguments> provideSuccessArguments() {
            return Stream.of(
                    Arguments.of(new ConditionalsTest.InputObj(1, 2), true),
                    Arguments.of(new ConditionalsTest.InputObj(1, 1), false),
                    Arguments.of(new ConditionalsTest.InputObj("hi", "hi"), false),
                    Arguments.of(new ConditionalsTest.InputObj("a", "b"), true),
                    Arguments.of(new ConditionalsTest.InputObj(1.0, 1.0), false),
                    Arguments.of(new ConditionalsTest.InputObj(5, 4), false));
        }
    }

    @Nested
    class LessThanEquals {
        @ParameterizedTest
        @MethodSource("provideSuccessArguments")
        void shouldCompleteLessThanEqualsWorkflowWithConditionals(InputObj inputObject, boolean expectedOutput) {
            workflowVerifier
                    .prepareRun(workflowLessThanEquals, Arg.of("input", inputObject))
                    .waitForStatus(LHStatus.COMPLETED)
                    .thenVerifyVariable(0, "result", variableValue -> assertThat(variableValue.getBool())
                            .isEqualTo(expectedOutput))
                    .start();
        }

        private static Stream<Arguments> provideSuccessArguments() {
            return Stream.of(
                    Arguments.of(new ConditionalsTest.InputObj(1, 2), true),
                    Arguments.of(new ConditionalsTest.InputObj(1, 1), true),
                    Arguments.of(new ConditionalsTest.InputObj("hi", "hi"), true),
                    Arguments.of(new ConditionalsTest.InputObj("a", "b"), true),
                    Arguments.of(new ConditionalsTest.InputObj(1.0, 1.0), true),
                    Arguments.of(new ConditionalsTest.InputObj(5, 4), false));
        }
    }

    @Nested
    class GreaterThan {
        @ParameterizedTest
        @MethodSource("provideSuccessArguments")
        void shouldCompleteGreaterThanWorkflowWithConditionals(InputObj inputObject, boolean expectedOutput) {
            workflowVerifier
                    .prepareRun(workflowGreaterThan, Arg.of("input", inputObject))
                    .waitForStatus(LHStatus.COMPLETED)
                    .thenVerifyVariable(0, "result", variableValue -> assertThat(variableValue.getBool())
                            .isEqualTo(expectedOutput))
                    .start();
        }

        private static Stream<Arguments> provideSuccessArguments() {
            return Stream.of(
                    Arguments.of(new ConditionalsTest.InputObj(1, 2), false),
                    Arguments.of(new ConditionalsTest.InputObj(1, 1), false),
                    Arguments.of(new ConditionalsTest.InputObj(2, 1), true),
                    Arguments.of(new ConditionalsTest.InputObj("hi", "hi"), false),
                    Arguments.of(new ConditionalsTest.InputObj("a", "b"), false),
                    Arguments.of(new ConditionalsTest.InputObj("b", "a"), true),
                    Arguments.of(new ConditionalsTest.InputObj(5.4, 4.0), true));
        }
    }

    @Nested
    class GreaterThanEquals {
        @ParameterizedTest
        @MethodSource("provideSuccessArguments")
        void shouldCompleteGreaterThanEqualsWorkflowWithConditionals(InputObj inputObject, boolean expectedOutput) {
            workflowVerifier
                    .prepareRun(workflowGreaterThanEquals, Arg.of("input", inputObject))
                    .waitForStatus(LHStatus.COMPLETED)
                    .thenVerifyTaskRunResult(0, 1, variableValue -> assertThat(variableValue.getBool())
                            .isEqualTo(true))
                    .thenVerifyTaskRunResult(0, 3, variableValue -> assertThat(variableValue.getBool())
                            .isEqualTo(expectedOutput))
                    .start();
        }

        private static Stream<Arguments> provideSuccessArguments() {
            return Stream.of(
                    Arguments.of(new ConditionalsTest.InputObj(1, 2), false),
                    Arguments.of(new ConditionalsTest.InputObj(1, 1), true),
                    Arguments.of(new ConditionalsTest.InputObj(2, 1), true),
                    Arguments.of(new ConditionalsTest.InputObj("hi", "hi"), true),
                    Arguments.of(new ConditionalsTest.InputObj("a", "b"), false),
                    Arguments.of(new ConditionalsTest.InputObj("b", "a"), true),
                    Arguments.of(new ConditionalsTest.InputObj(5.4, 4.0), true));
        }
    }

    @Nested
    class IsIn {
        @ParameterizedTest
        @MethodSource("provideSuccessArguments")
        void shouldCompleteIsInWorkflowWithConditionals(InputObj inputObject, boolean expectedOutput) {
            workflowVerifier
                    .prepareRun(workflowIsIn, Arg.of("input", inputObject))
                    .waitForStatus(LHStatus.COMPLETED)
                    .thenVerifyVariable(0, "result", variableValue -> assertThat(variableValue.getBool())
                            .isEqualTo(expectedOutput))
                    .start();
        }

        private static Stream<Arguments> provideSuccessArguments() {
            return Stream.of(
                    Arguments.of(new ConditionalsTest.InputObj(Map.of("a", 1), Map.of("a", 1)), false),
                    Arguments.of(new ConditionalsTest.InputObj("hi", Map.of("hi", 2)), true),
                    Arguments.of(new ConditionalsTest.InputObj(2, Map.of("hi", 2)), false),
                    Arguments.of(
                            new ConditionalsTest.InputObj(Arrays.asList(0), Arrays.asList(0)),
                            false), // Will check for '[0]'
                    Arguments.of(new ConditionalsTest.InputObj(0, Arrays.asList(0)), true),
                    Arguments.of(new ConditionalsTest.InputObj(1, "one"), false),
                    Arguments.of(new ConditionalsTest.InputObj("o", "one"), true),
                    Arguments.of(new ConditionalsTest.InputObj(2, "2"), true),
                    Arguments.of(new ConditionalsTest.InputObj(2, Map.of("a", 1)), false));
        }
    }

    @Nested
    class NotIn {
        @ParameterizedTest
        @MethodSource("provideSuccessArguments")
        void shouldCompleteNotInWorkflowWithConditionals(InputObj inputObject, boolean expectedOutput) {
            workflowVerifier
                    .prepareRun(workflowNotIn, Arg.of("input", inputObject))
                    .waitForStatus(LHStatus.COMPLETED)
                    .thenVerifyVariable(0, "result", variableValue -> assertThat(variableValue.getBool())
                            .isEqualTo(expectedOutput))
                    .start();
        }

        @Test
        void shouldFailNotInWorkflowWithInvalidArguments() {
            InputObj inputObject = new InputObj(1, 1.0);
            workflowVerifier
                    .prepareRun(workflowNotIn, Arg.of("input", inputObject))
                    .waitForStatus(LHStatus.ERROR)
                    .start();
        }

        private static Stream<Arguments> provideSuccessArguments() {
            return Stream.of(
                    Arguments.of(new ConditionalsTest.InputObj(Map.of("a", 1), Map.of("a", 1)), true),
                    Arguments.of(new ConditionalsTest.InputObj("hi", Map.of("hi", 2)), false),
                    Arguments.of(new ConditionalsTest.InputObj(2, Map.of("hi", 2)), true),
                    Arguments.of(
                            new ConditionalsTest.InputObj(Arrays.asList(0), Arrays.asList(0)),
                            true), // Will check for '[0]'
                    Arguments.of(new ConditionalsTest.InputObj(0, Arrays.asList(0)), false),
                    Arguments.of(new ConditionalsTest.InputObj(1, "one"), true),
                    Arguments.of(new ConditionalsTest.InputObj("o", "one"), false),
                    Arguments.of(new ConditionalsTest.InputObj(2, "2"), false),
                    Arguments.of(new ConditionalsTest.InputObj(2, Map.of("a", 1)), true));
        }
    }

    @Nested
    class DoesContain {
        @ParameterizedTest
        @MethodSource("provideSuccessArguments")
        void shouldCompleteDoesContainWorkflowWithConditionals(InputObj inputObject, boolean expectedOutput) {
            workflowVerifier
                    .prepareRun(workflowDoesContain, Arg.of("input", inputObject))
                    .waitForStatus(LHStatus.COMPLETED)
                    .thenVerifyVariable(0, "result", variableValue -> assertThat(variableValue.getBool())
                            .isEqualTo(expectedOutput))
                    .start();
        }

        private static Stream<Arguments> provideSuccessArguments() {
            // lhs is the collection/string, rhs is the element to search for
            return Stream.of(
                    Arguments.of(new ConditionalsTest.InputObj(Map.of("a", 1), Map.of("a", 1)), false),
                    Arguments.of(new ConditionalsTest.InputObj(Map.of("hi", 2), "hi"), true),
                    Arguments.of(new ConditionalsTest.InputObj(Map.of("hi", 2), 2), false),
                    Arguments.of(
                            new ConditionalsTest.InputObj(Arrays.asList(0), Arrays.asList(0)),
                            false), // Will check for '[0]'
                    Arguments.of(new ConditionalsTest.InputObj(Arrays.asList(0), 0), true),
                    Arguments.of(new ConditionalsTest.InputObj("one", 1), false),
                    Arguments.of(new ConditionalsTest.InputObj("one", "o"), true),
                    Arguments.of(new ConditionalsTest.InputObj("2", 2), true),
                    Arguments.of(new ConditionalsTest.InputObj(Map.of("a", 1), 2), false));
        }
    }

    @Nested
    class DoesNotContain {
        @ParameterizedTest
        @MethodSource("provideSuccessArguments")
        void shouldCompleteDoesNotContainWorkflowWithConditionals(InputObj inputObject, boolean expectedOutput) {
            workflowVerifier
                    .prepareRun(workflowDoesNotContain, Arg.of("input", inputObject))
                    .waitForStatus(LHStatus.COMPLETED)
                    .thenVerifyVariable(0, "result", variableValue -> assertThat(variableValue.getBool())
                            .isEqualTo(expectedOutput))
                    .start();
        }

        @Test
        void shouldFailDoesNotContainWorkflowWithInvalidArguments() {
            InputObj inputObject = new InputObj(1.0, 1);
            workflowVerifier
                    .prepareRun(workflowDoesNotContain, Arg.of("input", inputObject))
                    .waitForStatus(LHStatus.ERROR)
                    .start();
        }

        private static Stream<Arguments> provideSuccessArguments() {
            // lhs is the collection/string, rhs is the element to search for
            return Stream.of(
                    Arguments.of(new ConditionalsTest.InputObj(Map.of("a", 1), Map.of("a", 1)), true),
                    Arguments.of(new ConditionalsTest.InputObj(Map.of("hi", 2), "hi"), false),
                    Arguments.of(new ConditionalsTest.InputObj(Map.of("hi", 2), 2), true),
                    Arguments.of(
                            new ConditionalsTest.InputObj(Arrays.asList(0), Arrays.asList(0)),
                            true), // Will check for '[0]'
                    Arguments.of(new ConditionalsTest.InputObj(Arrays.asList(0), 0), false),
                    Arguments.of(new ConditionalsTest.InputObj("one", 1), true),
                    Arguments.of(new ConditionalsTest.InputObj("one", "o"), false),
                    Arguments.of(new ConditionalsTest.InputObj("2", 2), false),
                    Arguments.of(new ConditionalsTest.InputObj(Map.of("a", 1), 2), true));
        }
    }

    @Nested
    class BooleanVariable {
        @Test
        void shouldExecuteIfBlockWhenBoolVarIsTrue() {
            workflowVerifier
                    .prepareRun(workflowBoolVarDoIf, Arg.of("my-bool", true))
                    .waitForStatus(LHStatus.COMPLETED)
                    .thenVerifyVariable(0, "result", variableValue -> assertThat(variableValue.getBool())
                            .isTrue())
                    .start();
        }

        @Test
        void shouldSkipIfBlockWhenBoolVarIsFalse() {
            workflowVerifier
                    .prepareRun(workflowBoolVarDoIf, Arg.of("my-bool", false))
                    .waitForStatus(LHStatus.COMPLETED)
                    .thenVerifyVariable(0, "result", variableValue -> assertThat(variableValue.hasBool())
                            .isFalse())
                    .start();
        }

        @Test
        void shouldExecuteIfBlockWhenBoolVarIsTrueWithIfElse() {
            workflowVerifier
                    .prepareRun(workflowBoolVarDoIfElse, Arg.of("my-bool", true))
                    .waitForStatus(LHStatus.COMPLETED)
                    .thenVerifyVariable(0, "result", variableValue -> assertThat(variableValue.getBool())
                            .isTrue())
                    .start();
        }

        @Test
        void shouldExecuteElseBlockWhenBoolVarIsFalseWithIfElse() {
            workflowVerifier
                    .prepareRun(workflowBoolVarDoIfElse, Arg.of("my-bool", false))
                    .waitForStatus(LHStatus.COMPLETED)
                    .thenVerifyVariable(0, "result", variableValue -> assertThat(variableValue.getBool())
                            .isFalse())
                    .start();
        }
    }

    @Test
    void testThatWholeIfBlockIsSkipped() {
        workflowVerifier
                .prepareRun(workflowNestedIf, Arg.of("input", 16))
                .waitForStatus(LHStatus.COMPLETED)
                .verifyAllTaskRunOutputs(List.of(3))
                .start();
    }

    @Test
    void testThatInnerIfBlockIsSkipped() {
        workflowVerifier
                .prepareRun(workflowNestedIf, Arg.of("input", 11))
                .waitForStatus(LHStatus.COMPLETED)
                .verifyAllTaskRunOutputs(List.of(2, 3))
                .start();
    }

    @Test
    void testThatBothIfBlocksFire() {
        workflowVerifier
                .prepareRun(workflowNestedIf, Arg.of("input", 1))
                .waitForStatus(LHStatus.COMPLETED)
                .verifyAllTaskRunOutputs(List.of(1, 2, 3))
                .start();
    }

    @LHWorkflow("test-nested-if")
    public Workflow getNestedIfWorkflowImpl() {
        return new WorkflowImpl("test-nested-if", wf -> {
            WfRunVariable input = wf.addVariable("input", VariableType.INT);

            /*
            if (input < 15) {
                if (input < 10) {
                    execute(1);
                }
                execute(2);
            }
            execute(3);
             */
            wf.doIf(input.isLessThan(15), ifBlock -> {
                wf.doIf(input.isLessThan(10), ifBlock2 -> {
                    ifBlock2.execute("echo", 1);
                });
                ifBlock.execute("echo", 2);
            });

            wf.execute("echo", 3);
        });
    }

    @Test
    void ifElseShouldGoDownFirstPathWhenLessThan10() {
        workflowVerifier
                .prepareRun(workflowNestedIfElse, Arg.of("input", 1))
                .waitForStatus(LHStatus.COMPLETED)
                .verifyAllTaskRunOutputs(List.of(1, 4))
                .start();
    }

    @Test
    void ifElseGoesToElseThenNestedIfGoesToTrue() {
        workflowVerifier
                .prepareRun(workflowNestedIfElse, Arg.of("input", 11))
                .waitForStatus(LHStatus.COMPLETED)
                .verifyAllTaskRunOutputs(List.of(2, 3, 4))
                .start();
    }

    @Test
    void ifElseGoesToElseThenNestedIfGoesTofalse() {
        workflowVerifier
                .prepareRun(workflowNestedIfElse, Arg.of("input", 16))
                .waitForStatus(LHStatus.COMPLETED)
                .verifyAllTaskRunOutputs(List.of(3, 4))
                .start();
    }

    @LHWorkflow("test-nested-if-else")
    public Workflow getNestedIfElseWorkflow() {
        return new WorkflowImpl("test-nested-if-else", wf -> {
            WfRunVariable input = wf.addVariable("input", VariableType.INT);

            /*
            if (input < 10) {
                execute(1);
            } else {
                if (input < 15) {
                    execute(2);
                }
                execute(3);
            }
             */
            wf.doIfElse(
                    input.isLessThan(10),
                    ifBlock -> {
                        ifBlock.execute("echo", 1);
                    },
                    elseBlock -> {
                        wf.doIf(input.isLessThan(15), ifBlock -> {
                            ifBlock.execute("echo", 2);
                        });
                        elseBlock.execute("echo", 3);
                    });

            wf.execute("echo", 4);
        });
    }

    @LHWorkflow("test-conditionals-equals-workflow")
    public Workflow getEqualsWorkflowImpl() {
        return new WorkflowImpl("test-conditionals-equals-workflow", thread -> {
            // Use an input JSON blob with two fields, LHS and RHS.
            // This allows us to test with various types on the left and the
            // right, since right now the JSON_OBJ var type does not have a
            // schema.
            WfRunVariable input = thread.addVariable("input", VariableType.JSON_OBJ);

            // So that the run request succeeds even on workflows where we want
            // a crash.
            thread.execute("ag-one");

            thread.doIfElse(
                    input.jsonPath("$.lhs").isEqualTo(input.jsonPath("$.rhs")),
                    ifBlock -> {
                        ifBlock.execute("ag-one");
                    },
                    elseBlock -> {
                        elseBlock.execute("ag-two");
                    });
        });
    }

    @LHWorkflow("test-conditionals-struct-equals-workflow")
    public Workflow getStructEqualsWorkflowImpl() {
        return new WorkflowImpl("test-conditionals-struct-equals-workflow", thread -> {
            WfRunVariable structA = thread.declareStruct("struct-a", Car.class);
            WfRunVariable structB = thread.declareStruct("struct-b", Car.class);

            // So that the run request succeeds even on workflows where we want
            // a crash.
            thread.execute("ag-one");

            thread.doIfElse(
                    structA.isEqualTo(structB),
                    ifBlock -> {
                        ifBlock.execute("ag-one");
                    },
                    elseBlock -> {
                        elseBlock.execute("ag-two");
                    });
        });
    }

    @LHWorkflow("test-conditionals-not-equals-workflow")
    public Workflow getNotEqualsWorkflowImpl() {
        return new WorkflowImpl("test-conditionals-not-equals-workflow", thread -> {
            // Use an input JSON blob with two fields, LHS and RHS.
            // This allows us to test with various types on the left and the
            // right, since right now the JSON_OBJ var type does not have a
            // schema.
            WfRunVariable input = thread.addVariable("input", VariableType.JSON_OBJ);

            // So that the run request succeeds even on workflows where we want
            // a crash.
            thread.execute("ag-one");

            thread.doIfElse(
                    input.jsonPath("$.lhs").isNotEqualTo(input.jsonPath("$.rhs")),
                    ifBlock -> {
                        ifBlock.execute("ag-one");
                    },
                    elseBlock -> {
                        elseBlock.execute("ag-two");
                    });
        });
    }

    @LHWorkflow("test-conditionals-less-than-workflow")
    public Workflow getLessThanWorkflow() {
        return new WorkflowImpl("test-conditionals-less-than-workflow", thread -> {
            // Use an input JSON blob with two fields, LHS and RHS.
            // This allows us to test with various types on the left and the
            // right, since right now the JSON_OBJ var type does not have a
            // schema.
            WfRunVariable input = thread.addVariable("input", VariableType.JSON_OBJ);

            // So that the run request succeeds even on workflows where we want
            // a crash.
            thread.execute("ag-one");

            thread.doIfElse(
                    input.jsonPath("$.lhs").isLessThan(input.jsonPath("$.rhs")),
                    ifBlock -> {
                        ifBlock.execute("ag-one");
                    },
                    elseBlock -> {
                        elseBlock.execute("ag-two");
                    });
        });
    }

    @LHWorkflow("test-conditionals-less-than-equals-workflow")
    public Workflow getLessThanEqualsWorkflow() {
        return new WorkflowImpl("test-conditionals-less-than-equals-workflow", thread -> {
            // Use an input JSON blob with two fields, LHS and RHS.
            // This allows us to test with various types on the left and the
            // right, since right now the JSON_OBJ var type does not have a
            // schema.
            WfRunVariable input = thread.addVariable("input", VariableType.JSON_OBJ);

            WfRunVariable result = thread.declareBool("result");

            thread.doIfElse(
                    input.jsonPath("$.lhs").isLessThanEq(input.jsonPath("$.rhs")),
                    ifBlock -> {
                        result.assign(true);
                    },
                    elseBlock -> {
                        result.assign(false);
                    });
        });
    }

    @LHWorkflow("test-conditionals-greater-than-workflow")
    public Workflow getGreaterThanWorkflow() {
        return new WorkflowImpl("test-conditionals-greater-than-workflow", thread -> {
            // Use an input JSON blob with two fields, LHS and RHS.
            // This allows us to test with various types on the left and the
            // right, since right now the JSON_OBJ var type does not have a
            // schema.
            WfRunVariable input = thread.addVariable("input", VariableType.JSON_OBJ);

            WfRunVariable result = thread.declareBool("result");

            thread.doIfElse(
                    input.jsonPath("$.lhs").isGreaterThan(input.jsonPath("$.rhs")),
                    ifBlock -> {
                        result.assign(true);
                    },
                    elseBlock -> {
                        result.assign(false);
                    });
        });
    }

    @LHWorkflow("test-conditionals-greater-than-equals-workflow")
    public Workflow getGreaterThanEqualsWorkflow() {
        return new WorkflowImpl("test-conditionals-greater-than-equals-workflow", thread -> {
            // Use an input JSON blob with two fields, LHS and RHS.
            // This allows us to test with various types on the left and the
            // right, since right now the JSON_OBJ var type does not have a
            // schema.
            WfRunVariable input = thread.addVariable("input", VariableType.JSON_OBJ);

            // So that the run request succeeds even on workflows where we want
            // a crash.
            thread.execute("ag-one");

            thread.doIfElse(
                    input.jsonPath("$.lhs").isGreaterThanEq(input.jsonPath("$.rhs")),
                    ifBlock -> {
                        ifBlock.execute("ag-one");
                    },
                    elseBlock -> {
                        elseBlock.execute("ag-two");
                    });
        });
    }

    @LHWorkflow("test-conditionals-is-in-workflow")
    public Workflow getIsInWorkflow() {
        return new WorkflowImpl("test-conditionals-is-in-workflow", thread -> {
            // Use an input JSON blob with two fields, LHS and RHS.
            // This allows us to test with various types on the left and the
            // right, since right now the JSON_OBJ var type does not have a
            // schema.
            WfRunVariable input = thread.addVariable("input", VariableType.JSON_OBJ);

            WfRunVariable result = thread.declareBool("result");

            thread.doIfElse(
                    input.jsonPath("$.lhs").isIn(input.jsonPath("$.rhs")),
                    ifBlock -> {
                        result.assign(true);
                    },
                    elseBlock -> {
                        result.assign(false);
                    });
        });
    }

    @LHWorkflow("test-conditionals-not-in-workflow")
    public Workflow getNotInWorkflow() {
        return new WorkflowImpl("test-conditionals-not-in-workflow", thread -> {
            // Use an input JSON blob with two fields, LHS and RHS.
            // This allows us to test with various types on the left and the
            // right, since right now the JSON_OBJ var type does not have a
            // schema.
            WfRunVariable input = thread.addVariable("input", VariableType.JSON_OBJ);

            WfRunVariable result = thread.declareBool("result");

            thread.doIfElse(
                    input.jsonPath("$.lhs").isNotIn(input.jsonPath("$.rhs")),
                    ifBlock -> {
                        result.assign(true);
                    },
                    elseBlock -> {
                        result.assign(false);
                    });
        });
    }

    @LHWorkflow("test-conditionals-does-contain-workflow")
    public Workflow getDoesContainWorkflow() {
        return new WorkflowImpl("test-conditionals-does-contain-workflow", thread -> {
            // Use an input JSON blob with two fields, LHS and RHS.
            // LHS is the collection/string, RHS is the element to search for.
            WfRunVariable input = thread.addVariable("input", VariableType.JSON_OBJ);
            WfRunVariable result = thread.declareBool("result");
            thread.doIfElse(
                    input.jsonPath("$.lhs").doesContain(input.jsonPath("$.rhs")),
                    ifBlock -> {
                        result.assign(true);
                    },
                    elseBlock -> {
                        result.assign(false);
                    });
        });
    }

    @LHWorkflow("test-conditionals-does-not-contain-workflow")
    public Workflow getDoesNotContainWorkflow() {
        return new WorkflowImpl("test-conditionals-does-not-contain-workflow", thread -> {
            // Use an input JSON blob with two fields, LHS and RHS.
            // LHS is the collection/string, RHS is the element to search for.
            WfRunVariable input = thread.addVariable("input", VariableType.JSON_OBJ);
            WfRunVariable result = thread.declareBool("result");
            thread.doIfElse(
                    input.jsonPath("$.lhs").doesNotContain(input.jsonPath("$.rhs")),
                    ifBlock -> {
                        result.assign(true);
                    },
                    elseBlock -> {
                        result.assign(false);
                    });
        });
    }

    @LHWorkflow("test-conditionals-bool-var-do-if")
    public Workflow getBoolVarDoIfWorkflow() {
        return new WorkflowImpl("test-conditionals-bool-var-do-if", thread -> {
            WfRunVariable myBool = thread.declareBool("my-bool");
            WfRunVariable result = thread.declareBool("result");
            thread.doIf(myBool, ifBlock -> {
                result.assign(true);
            });
        });
    }

    @LHWorkflow("test-conditionals-bool-var-do-if-else")
    public Workflow getBoolVarDoIfElseWorkflow() {
        return new WorkflowImpl("test-conditionals-bool-var-do-if-else", thread -> {
            WfRunVariable myBool = thread.declareBool("my-bool");
            WfRunVariable result = thread.declareBool("result");
            thread.doIfElse(
                    myBool,
                    ifBlock -> {
                        result.assign(true);
                    },
                    elseBlock -> {
                        result.assign(false);
                    });
        });
    }

    @LHTaskMethod("ag-one")
    public boolean one() {
        return true;
    }

    @LHTaskMethod("ag-two")
    public boolean two() {
        return false;
    }

    @LHTaskMethod("echo")
    public int echo(int input) {
        return input;
    }

    static class InputObj {

        public Object lhs;
        public Object rhs;

        public InputObj() {}

        public InputObj(Object lhs, Object rhs) {
            this.lhs = lhs;
            this.rhs = rhs;
        }

        @Override
        public String toString() {
            return "InputObj{" + "lhs=" + lhs + ", rhs=" + rhs + '}';
        }
    }
}
