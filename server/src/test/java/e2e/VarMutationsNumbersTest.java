package e2e;

import static org.assertj.core.api.Assertions.*;

import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.VariableMutationType;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.util.Arg;
import io.littlehorse.sdk.wfsdk.NodeOutput;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.LHWorkflow;
import io.littlehorse.test.WorkflowVerifier;
import org.junit.jupiter.api.Test;

@LHTest
public class VarMutationsNumbersTest {

    private WorkflowVerifier workflowVerifier;

    @LHWorkflow("test-mutation-workflow")
    private Workflow workflow;

    @Test
    void shouldApplyMathMutationsOverNumberVariables() {
        Arg myInt = Arg.of("my-int", 5);
        Arg myDouble = Arg.of("my-double", 24.2);
        workflowVerifier
                .prepareRun(workflow, myInt, myDouble)
                .waitForStatus(LHStatus.COMPLETED)
                .thenVerifyVariable(0, "my-int", variableValue -> assertThat(variableValue.getInt())
                        .isEqualTo(13))
                .thenVerifyVariable(0, "my-double", variableValue -> assertThat(variableValue.getDouble())
                        .isEqualTo(24.2))
                .thenVerifyVariable(0, "my-other-int", variableValue -> assertThat(variableValue.getInt())
                        .isEqualTo((int) (24.2 / 13)))
                .start();
    }

    @Test
    void shouldRollbackVariablesWhenMutationsFailed() {
        // this input will cause division by zero, which makes the WfRun fail with ERROR status.
        Arg myInt = Arg.of("my-int", -8);
        Arg myDouble = Arg.of("my-double", 10.0);
        workflowVerifier
                .prepareRun(workflow, myInt, myDouble)
                .waitForStatus(LHStatus.ERROR)
                .thenVerifyVariable(0, "my-int", variableValue -> assertThat(variableValue.getInt())
                        .isEqualTo(-8))
                .thenVerifyVariable(0, "my-double", variableValue -> assertThat(variableValue.getDouble())
                        .isEqualTo(10.0))
                .start();
    }

    @LHWorkflow("test-mutation-workflow")
    public Workflow getWorkflowImpl() {
        return new WorkflowImpl("test-mutation-workflow", thread -> {
            WfRunVariable myInt = thread.addVariable("my-int", VariableType.INT);

            WfRunVariable myDouble = thread.addVariable("my-double", VariableType.DOUBLE);

            WfRunVariable myOtherInt = thread.addVariable("my-other-int", VariableType.INT);

            NodeOutput output = thread.execute("ad-simple");
            thread.mutate(myInt, VariableMutationType.ADD, output);
            thread.mutate(myInt, VariableMutationType.SUBTRACT, 2);

            // ensure that we can cast from double to int, and that the
            // original type is respected
            thread.mutate(myOtherInt, VariableMutationType.ASSIGN, myDouble.castToInt());

            // Do some math, and divide by zero to show that failures work
            thread.mutate(myOtherInt, VariableMutationType.DIVIDE, myInt);
        });
    }

    @LHTaskMethod("ad-simple")
    public Integer obiWan() {
        return 10;
    }
}
