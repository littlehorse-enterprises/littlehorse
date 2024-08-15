package e2e;

import io.littlehorse.common.LHConstants;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.VarNameAndVal;
import io.littlehorse.sdk.common.proto.VariableMutationType;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.sdk.common.util.Arg;
import io.littlehorse.sdk.wfsdk.TaskNodeOutput;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.sdk.worker.LHType;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.LHWorkflow;
import io.littlehorse.test.WorkflowVerifier;
import java.util.List;
import java.util.Objects;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

@LHTest
public class VariablesTest {

    @LHWorkflow("masked-variables-wf")
    private Workflow maskedVariablesWf;

    private WorkflowVerifier workflowVerifier;

    @Test
    public void shouldMaskVariableValues() {
        final String inputText =
                """
                Vestibulum id mauris vel ex pharetra facilisis. Sed egestas metus mi,\s
                non interdum mauris blandit vitae. Aliquam odio tortor, sollicitudin\s
                nec aliquet mollis, mollis sit amet arcu. In hac habitasse platea dictumst.\s
                Vestibulum lobortis euismod elementum. Nam sed porttitor massa.
               \s""";
        workflowVerifier
                .prepareRun(maskedVariablesWf, Arg.of("text", inputText))
                .waitForStatus(LHStatus.COMPLETED)
                .thenVerifyVariable(0, "text", variableValue -> {
                    Assertions.assertThat(variableValue.getStr())
                            .isNotEqualTo(inputText)
                            .isEqualTo(LHConstants.STRING_MASK);
                })
                .thenVerifyVariable(0, "length", variableValue -> {
                    Assertions.assertThat(variableValue.getInt()).isEqualTo(inputText.length());
                })
                .thenVerifyTaskRun(0, 1, taskRun -> {
                    List<VarNameAndVal> variables = taskRun.getInputVariablesList();
                    Assertions.assertThat(variables).hasSize(1);
                    VarNameAndVal taskArg = variables.get(0);
                    VariableValue inputTextArg = taskArg.getValue();
                    Assertions.assertThat(inputTextArg.getStr()).isEqualTo(LHConstants.STRING_MASK);
                })
                .start();
    }

    @LHWorkflow("masked-variables-wf")
    public Workflow buildChildWf() {
        return new WorkflowImpl("masked-variables-wf", thread -> {
            WfRunVariable textVariable =
                    thread.addVariable("text", VariableType.STR).masked().required();
            TaskNodeOutput lengthNodeOutput = thread.execute("get-text-length", textVariable);
            WfRunVariable length = thread.addVariable("length", VariableType.INT);
            thread.mutate(length, VariableMutationType.ASSIGN, lengthNodeOutput);
            thread.execute("print-length", length);
        });
    }

    @LHTaskMethod("get-text-length")
    @LHType(masked = false, name = "input-text-length")
    public Integer verifyOutput(@LHType(masked = true, name = "input-text") String variableValue) {
        Objects.requireNonNull(variableValue);
        return variableValue.length();
    }

    @LHTaskMethod("print-length")
    public void printLength(Integer length) {
        System.out.println("Text length is " + length);
    }
}
