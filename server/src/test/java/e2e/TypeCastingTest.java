package e2e;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.grpc.Status.Code;
import io.grpc.StatusRuntimeException;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.TaskStatus;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.util.Arg;
import io.littlehorse.sdk.wfsdk.NodeOutput;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.LHWorkflow;
import io.littlehorse.test.WithWorkers;
import io.littlehorse.test.WorkflowVerifier;
import org.junit.jupiter.api.Test;

@LHTest
@WithWorkers("castingWorker")
public class TypeCastingTest {

    private LittleHorseBlockingStub client;
    private WorkflowVerifier verifier;

    @LHWorkflow("automatic-casting-wf")
    private Workflow automaticCastingWorkflow;

    @LHWorkflow("manual-casting-wf")
    private Workflow manualCastingWorkflow;

    @LHWorkflow("variable-assignment-casting-wf")
    private Workflow variableAssignmentCastingWorkflow;

    @LHWorkflow("complex-casting-chains-wf")
    private Workflow complexCastingChainsWorkflow;

    @Test
    void shouldPerformAutomaticCastingIntToStr() {
        verifier.prepareRun(automaticCastingWorkflow, Arg.of("int-var", 42))
                .waitForStatus(LHStatus.COMPLETED)
                .waitForTaskStatus(0, 1, TaskStatus.TASK_SUCCESS)
                .thenVerifyTaskRunResult(0, 1, result -> assertEquals("processed-42", result.getStr()))
                .start();
    }

    @Test
    void shouldPerformAutomaticCastingIntToDouble() {
        verifier.prepareRun(automaticCastingWorkflow, Arg.of("int-var", 42))
                .waitForStatus(LHStatus.COMPLETED)
                .waitForTaskStatus(0, 2, TaskStatus.TASK_SUCCESS)
                .thenVerifyTaskRunResult(0, 2, result -> assertEquals(63.0, result.getDouble(), 0.001))
                .start();
    }

    @Test
    void shouldPerformAutomaticCastingDoubleToStr() {
        verifier.prepareRun(automaticCastingWorkflow, Arg.of("double-var", 3.14))
                .waitForStatus(LHStatus.COMPLETED)
                .waitForTaskStatus(0, 3, TaskStatus.TASK_SUCCESS)
                .thenVerifyTaskRunResult(0, 3, result -> assertEquals("processed-3.14", result.getStr()))
                .start();
    }

    @Test
    void shouldPerformAutomaticCastingBoolToStr() {
        verifier.prepareRun(automaticCastingWorkflow, Arg.of("bool-var", true))
                .waitForStatus(LHStatus.COMPLETED)
                .waitForTaskStatus(0, 4, TaskStatus.TASK_SUCCESS)
                .thenVerifyTaskRunResult(0, 4, result -> assertEquals("processed-true", result.getStr()))
                .start();
    }

    @Test
    void shouldPerformManualCastingStrToInt() {
        verifier.prepareRun(manualCastingWorkflow, Arg.of("str-var", "123"))
                .waitForStatus(LHStatus.COMPLETED)
                .waitForTaskStatus(0, 1, TaskStatus.TASK_SUCCESS)
                .thenVerifyTaskRunResult(0, 1, result -> assertEquals(246, result.getInt()))
                .start();
    }

    @Test
    void shouldPerformManualCastingStrToDouble() {
        verifier.prepareRun(manualCastingWorkflow, Arg.of("str-double-var", "123.45"))
                .waitForStatus(LHStatus.COMPLETED)
                .waitForTaskStatus(0, 2, TaskStatus.TASK_SUCCESS)
                .thenVerifyTaskRunResult(0, 2, result -> assertEquals(185.175, result.getDouble(), 0.001))
                .start();
    }

    @Test
    void shouldPerformManualCastingStrToBool() {
        verifier.prepareRun(manualCastingWorkflow, Arg.of("bool-str-var", "true"))
                .waitForStatus(LHStatus.COMPLETED)
                .waitForTaskStatus(0, 3, TaskStatus.TASK_SUCCESS)
                .thenVerifyTaskRunResult(0, 3, result -> assertEquals(false, result.getBool()))
                .start();
    }

    @Test
    void shouldPerformManualCastingDoubleToInt() {
        verifier.prepareRun(manualCastingWorkflow, Arg.of("double-var", 123.67))
                .waitForStatus(LHStatus.COMPLETED)
                .waitForTaskStatus(0, 4, TaskStatus.TASK_SUCCESS)
                .thenVerifyTaskRunResult(0, 4, result -> assertEquals(246, result.getInt()))
                .start();
    }

    @Test
    void shouldPerformVariableAssignmentWithCasting() {
        verifier.prepareRun(variableAssignmentCastingWorkflow, Arg.of("input-str", "456"))
                .waitForStatus(LHStatus.COMPLETED)
                .waitForTaskStatus(0, 1, TaskStatus.TASK_SUCCESS)
                .thenVerifyTaskRunResult(0, 1, result -> assertEquals(912, result.getInt()))
                .start();
    }

    @Test
    void shouldHandleComplexCastingChains() {
        verifier.prepareRun(complexCastingChainsWorkflow, Arg.of("initial-str", "100"))
                .waitForStatus(LHStatus.COMPLETED)
                .waitForTaskStatus(0, 4, TaskStatus.TASK_SUCCESS)
                .thenVerifyTaskRunResult(0, 4, result -> assertEquals("processed-600", result.getStr()))
                .start();
    }

    @Test
    void shouldFailUnsupportedCast() {
        Workflow invalidWorkflow = new WorkflowImpl("invalid-cast", wf -> {
            WfRunVariable doubleVar = wf.addVariable("double-var", VariableType.DOUBLE);
            WfRunVariable boolVar = wf.addVariable("bool-var", VariableType.BOOL);
            boolVar.assign(doubleVar.cast(VariableType.BOOL));
        });

        assertThatThrownBy(() -> {
                    invalidWorkflow.registerWfSpec(client);
                    verifier.prepareRun(invalidWorkflow, Arg.of("double-var", 3.14))
                            .waitForStatus(LHStatus.ERROR)
                            .start();
                })
                .matches(
                        exn -> {
                            StatusRuntimeException sre = (StatusRuntimeException) exn;
                            return sre.getStatus().getCode() == Code.INVALID_ARGUMENT
                                    && (sre.getMessage()
                                            .contains(
                                                    "Cannot cast from DOUBLE to BOOL. This conversion is not supported."));
                        },
                        "should throw INVALID_ARGUMENT with unsupported DOUBLE to BOOL cast message");
    }

    @Test
    void shouldFailWorkflowRegistrationForUnsupportedIntToBoolCast() {
        Workflow invalidWorkflow = new WorkflowImpl("invalid-int-bool-cast", wf -> {
            WfRunVariable intVar = wf.addVariable("int-var", VariableType.INT);
            wf.execute("casting-bool-task", intVar.cast(VariableType.BOOL));
        });

        assertThatThrownBy(() -> invalidWorkflow.registerWfSpec(client))
                .matches(
                        exn -> {
                            StatusRuntimeException sre = (StatusRuntimeException) exn;
                            return sre.getStatus().getCode() == Code.INVALID_ARGUMENT
                                    && sre.getMessage()
                                            .contains(
                                                    "Cannot cast from INT to BOOL. This conversion is not supported.");
                        },
                        "should throw INVALID_ARGUMENT with unsupported INT to BOOL cast message");
    }

    @Test
    void shouldFailWorkflowRegistrationForMissingManualCast() {
        Workflow invalidWorkflow = new WorkflowImpl("missing-manual-cast", wf -> {
            WfRunVariable strVar = wf.addVariable("str-var", VariableType.STR);
            wf.execute("casting-int-task", strVar);
        });

        assertThatThrownBy(() -> invalidWorkflow.registerWfSpec(client)).matches(exn -> {
            StatusRuntimeException sre = (StatusRuntimeException) exn;
            return sre.getStatus().getCode() == Code.INVALID_ARGUMENT
                    && (sre.getMessage().contains("Cannot assign STR to INT without explicit casting")
                            || sre.getMessage().contains("Use .castToInt() or .cast(VariableType.INT) method")
                            || sre.getMessage().contains("cannot assign STR to INT"));
        });
    }

    @Test
    void shouldFailVariableAssignmentWithoutExplicitCast() {
        Workflow invalidWorkflow = new WorkflowImpl("invalid-var-assignment", wf -> {
            WfRunVariable strVar = wf.addVariable("str-var", VariableType.STR);
            WfRunVariable intVar = wf.addVariable("int-var", VariableType.INT);
            intVar.assign(strVar);
        });

        assertThatThrownBy(() -> invalidWorkflow.registerWfSpec(client)).matches(exn -> {
            StatusRuntimeException sre = (StatusRuntimeException) exn;
            return sre.getStatus().getCode() == Code.INVALID_ARGUMENT
                    && (sre.getMessage().contains("cannot use a STR as a INT")
                            || sre.getMessage().contains("Cannot assign STR to INT"));
        });
    }

    @LHWorkflow("automatic-casting-wf")
    public Workflow automaticCastingWorkflow() {
        return new WorkflowImpl("automatic-casting-wf", wf -> {
            WfRunVariable intVar = wf.addVariable("int-var", VariableType.INT).withDefault(42);
            WfRunVariable doubleVar =
                    wf.addVariable("double-var", VariableType.DOUBLE).withDefault(3.14);
            WfRunVariable boolVar =
                    wf.addVariable("bool-var", VariableType.BOOL).withDefault(true);

            wf.execute("casting-string-task", intVar);
            wf.execute("casting-double-task", intVar);
            wf.execute("casting-string-task", doubleVar);
            wf.execute("casting-string-task", boolVar);
        });
    }

    @LHWorkflow("manual-casting-wf")
    public Workflow manualCastingWorkflow() {
        return new WorkflowImpl("manual-casting-wf", wf -> {
            WfRunVariable strIntVar =
                    wf.addVariable("str-var", VariableType.STR).withDefault("3");
            WfRunVariable strDoubleVar =
                    wf.addVariable("str-double-var", VariableType.STR).withDefault("3.14");
            WfRunVariable doubleVar =
                    wf.addVariable("double-var", VariableType.DOUBLE).withDefault(123.67);

            wf.execute("casting-int-task", strIntVar.cast(VariableType.INT));
            wf.execute("casting-double-task", strDoubleVar.cast(VariableType.DOUBLE));

            WfRunVariable boolStrVar =
                    wf.addVariable("bool-str-var", VariableType.STR).withDefault("true");
            wf.execute("casting-bool-task", boolStrVar.cast(VariableType.BOOL));

            wf.execute("casting-int-task", doubleVar.cast(VariableType.INT));
        });
    }

    @LHWorkflow("variable-assignment-casting-wf")
    public Workflow variableAssignmentCastingWorkflow() {
        return new WorkflowImpl("variable-assignment-casting-wf", wf -> {
            WfRunVariable inputStr =
                    wf.addVariable("input-str", VariableType.STR).withDefault("456");
            WfRunVariable resultInt = wf.addVariable("result-int", VariableType.INT);

            resultInt.assign(inputStr.cast(VariableType.INT));

            wf.execute("casting-int-task", resultInt);
        });
    }

    @LHWorkflow("complex-casting-chains-wf")
    public Workflow complexCastingChainsWorkflow() {
        return new WorkflowImpl("complex-casting-chains-wf", wf -> {
            WfRunVariable initialStr =
                    wf.addVariable("initial-str", VariableType.STR).withDefault("100");

            NodeOutput step1 = wf.execute("casting-int-task", initialStr.cast(VariableType.INT));
            NodeOutput step2 = wf.execute("casting-double-task", step1);
            NodeOutput step3 = wf.execute("casting-int-task", step2.cast(VariableType.INT));
            wf.execute("casting-string-task", step3);
        });
    }

    public Object castingWorker() {
        return new CastingWorker();
    }

    public class CastingWorker {

        @LHTaskMethod("casting-string-task")
        public String stringTask(String value) {
            return "processed-" + value;
        }

        @LHTaskMethod("casting-int-task")
        public int intTask(int value) {
            return value * 2;
        }

        @LHTaskMethod("casting-double-task")
        public double doubleTask(double value) {
            return value * 1.5;
        }

        @LHTaskMethod("casting-bool-task")
        public boolean boolTask(boolean value) {
            return !value;
        }
    }
}
