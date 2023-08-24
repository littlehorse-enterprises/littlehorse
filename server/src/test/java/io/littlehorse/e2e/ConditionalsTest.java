package io.littlehorse.e2e;

import static org.assertj.core.api.Assertions.*;

import io.littlehorse.sdk.common.proto.Comparator;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.util.Arg;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.LHWorkflow;
import io.littlehorse.test.WorkflowVerifier;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@LHTest
public class ConditionalsTest {

    @LHWorkflow("test-conditionals-equals-workflow")
    private Workflow workflowEquals;

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

    private WorkflowVerifier workflowVerifier;

    @Nested
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
    }

    @Nested
    class NotEquals {
        @ParameterizedTest
        @MethodSource("provideSuccessArguments")
        void shouldCompleteEqualsWorkflowWithConditionals(InputObj inputObject, boolean expectedOutput) {
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
        void shouldCompleteLessThanWorkflowWithConditionals(InputObj inputObject, boolean expectedOutput) {
            workflowVerifier
                    .prepareRun(workflowLessThanEquals, Arg.of("input", inputObject))
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
                    .thenVerifyTaskRunResult(0, 1, variableValue -> assertThat(variableValue.getBool())
                            .isEqualTo(true))
                    .thenVerifyTaskRunResult(0, 3, variableValue -> assertThat(variableValue.getBool())
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
        void shouldCompleteGreaterThanWorkflowWithConditionals(InputObj inputObject, boolean expectedOutput) {
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
                    thread.condition(input.jsonPath("$.lhs"), Comparator.EQUALS, input.jsonPath("$.rhs")),
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
                    thread.condition(input.jsonPath("$.lhs"), Comparator.NOT_EQUALS, input.jsonPath("$.rhs")),
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
                    thread.condition(input.jsonPath("$.lhs"), Comparator.LESS_THAN, input.jsonPath("$.rhs")),
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

            // So that the run request succeeds even on workflows where we want
            // a crash.
            thread.execute("ag-one");

            thread.doIfElse(
                    thread.condition(input.jsonPath("$.lhs"), Comparator.LESS_THAN_EQ, input.jsonPath("$.rhs")),
                    ifBlock -> {
                        ifBlock.execute("ag-one");
                    },
                    elseBlock -> {
                        elseBlock.execute("ag-two");
                    });
        });
    }

    @LHWorkflow("test-conditionals-greater-than-workflow")
    public Workflow getGreaterThanWorkflow() {
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
                    thread.condition(input.jsonPath("$.lhs"), Comparator.GREATER_THAN, input.jsonPath("$.rhs")),
                    ifBlock -> {
                        ifBlock.execute("ag-one");
                    },
                    elseBlock -> {
                        elseBlock.execute("ag-two");
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
                    thread.condition(input.jsonPath("$.lhs"), Comparator.GREATER_THAN_EQ, input.jsonPath("$.rhs")),
                    ifBlock -> {
                        ifBlock.execute("ag-one");
                    },
                    elseBlock -> {
                        elseBlock.execute("ag-two");
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

    static class InputObj {

        public Object lhs;
        public Object rhs;

        public InputObj() {}

        public InputObj(Object lhs, Object rhs) {
            this.lhs = lhs;
            this.rhs = rhs;
        }
    }
}
