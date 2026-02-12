package e2e;

import static org.assertj.core.api.Assertions.*;

import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.exception.LHJsonProcessingException;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.Operation;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.sdk.common.util.Arg;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.LHWorkflow;
import io.littlehorse.test.WorkflowVerifier;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;

@LHTest
public class VarMutationListsTest {

    private WorkflowVerifier workflowVerifier;

    @LHWorkflow("workflow-test-lists")
    private Workflow workflow;

    @Test
    public void shouldCompleteRemoveListVariableMutations() {
        List<Object> workflowInput = Arrays.asList("asdf", "asdf", 3, 4, 9);
        List<Object> expectedOutput = Arrays.asList("asdf", "asdf", 3, 9);

        Consumer<VariableValue> verifyVariableListMutation = variableValue -> {
            try {
                @SuppressWarnings("unchecked")
                List<Object> variableList = LHLibUtil.deserializeFromjson(variableValue.getJsonArr(), List.class);
                assertThat(variableList).containsExactly(expectedOutput);
            } catch (LHJsonProcessingException exn) {
                throw new RuntimeException(exn);
            }
        };
        workflowVerifier
                .prepareRun(workflow, Arg.of("list-one", workflowInput))
                .waitForStatus(LHStatus.COMPLETED)
                .thenVerifyVariable(0, "list-one", verifyVariableListMutation);
    }

    @Test
    public void shouldThrowOutOfIndexException() {
        List<Object> workflowInput = Arrays.asList(5, "hello", 3, 4);
        Consumer<VariableValue> assertVariableValueRollback = variableValue -> {
            try {
                @SuppressWarnings("unchecked")
                List<Object> variableList = LHLibUtil.deserializeFromjson(variableValue.getJsonArr(), List.class);
                assertThat(variableList).containsExactly(workflowInput);
            } catch (LHJsonProcessingException exn) {
                throw new RuntimeException(exn);
            }
        };
        workflowVerifier
                .prepareRun(workflow, Arg.of("list-one", workflowInput))
                .waitForStatus(LHStatus.ERROR)
                .thenVerifyVariable(0, "list-one", assertVariableValueRollback);
    }

    @LHWorkflow("workflow-test-lists")
    public Workflow getWorkflowImpl() {
        return new WorkflowImpl("workflow-test-lists", thread -> {
            WfRunVariable listOne = thread.addVariable("list-one", VariableType.JSON_ARR);

            thread.execute("af-simple");

            thread.mutate(listOne, Operation.REMOVE_IF_PRESENT, 5);
            thread.mutate(listOne, Operation.REMOVE_IF_PRESENT, "hello");
            thread.mutate(listOne, Operation.REMOVE_INDEX, 3);
        });
    }
}
