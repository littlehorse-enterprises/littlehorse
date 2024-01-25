package e2e;

import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.VariableMutationType;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.WfRunId;
import io.littlehorse.sdk.common.proto.WfRunVariableAccessLevel;
import io.littlehorse.sdk.common.util.Arg;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.LHWorkflow;
import io.littlehorse.test.WorkflowVerifier;
import java.util.Objects;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

@LHTest
public class SharedVariablesTest {

    private WorkflowVerifier workflowVerifier;

    @LHWorkflow("shared-variables-parent-wf")
    private Workflow parentWf;

    @LHWorkflow("shared-variables-child-wf")
    private Workflow childWf;

    @Test
    public void shouldResolvePublicVariablesFromParentWf() {
        WfRunId parentWfRun = workflowVerifier
                .prepareRun(parentWf, Arg.of("input-number", 3))
                .waitForStatus(LHStatus.COMPLETED)
                .start(WfRunId.newBuilder().setId("parent-wf-run").build());

        workflowVerifier
                .prepareRun(childWf)
                .waitForStatus(LHStatus.COMPLETED)
                .thenVerifyVariable(0, "calculated-value", variableValue -> {
                    Assertions.assertThat(variableValue.getInt()).isEqualTo(12);
                })
                .start(WfRunId.newBuilder()
                        .setId("child-wf-run")
                        .setParentWfRunId(parentWfRun)
                        .build());
    }

    @LHWorkflow("shared-variables-parent-wf")
    public Workflow buildParentWf() {
        return new WorkflowImpl("shared-variables-parent-wf", thread -> {
            WfRunVariable inputNumber =
                    thread.addVariable("input-number", VariableType.INT).required();
            WfRunVariable publicVariable = thread.addVariable("public-variable", VariableType.INT)
                    .withAccessLevel(WfRunVariableAccessLevel.PUBLIC_VAR);
            thread.mutate(inputNumber, VariableMutationType.MULTIPLY, 2);
            thread.mutate(publicVariable, VariableMutationType.ASSIGN, inputNumber);
        });
    }

    @LHWorkflow("shared-variables-child-wf")
    public Workflow buildChildWf() {
        Workflow out = new WorkflowImpl("shared-variables-child-wf", thread -> {
            WfRunVariable publicVariable = thread.addVariable("public-variable", VariableType.INT)
                    .withAccessLevel(WfRunVariableAccessLevel.INHERITED_VAR);
            WfRunVariable calculatedValue =
                    thread.addVariable("calculated-value", VariableType.INT).searchable();
            thread.mutate(publicVariable, VariableMutationType.MULTIPLY, 2);
            thread.execute("print-output", publicVariable);
            thread.mutate(calculatedValue, VariableMutationType.ASSIGN, publicVariable);
        });
        out.setParent("shared-variables-parent-wf");
        return out;
    }

    @LHTaskMethod("print-output")
    public void verifyOutput(Integer output) {
        Objects.requireNonNull(output);
        System.out.println("Calculated value: " + output);
    }
}
