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
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.LHWorkflow;
import io.littlehorse.test.WorkflowVerifier;
import java.util.Map;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;

@LHTest
public class JsonMutationTest {

    private WorkflowVerifier workflowVerifier;

    @LHWorkflow("json-mutation-workflow")
    private Workflow workflow;

    @Test
    void shouldRemoveKeyFromJsonVar() {
        Arg workflowInputVariable = Arg.of("my-obj", Map.of("foo", "bar", "baz", 2));
        Consumer<VariableValue> verifyVariableDoesNotContainFooKey = variableValue -> {
            try {
                @SuppressWarnings("unchecked")
                Map<String, String> jsonMap = LHLibUtil.deserializeFromjson(variableValue.getJsonObj(), Map.class);
                assertThat(jsonMap).doesNotContainKey("foo");
            } catch (LHJsonProcessingException e) {
                throw new RuntimeException(e);
            }
        };
        workflowVerifier
                .prepareRun(workflow, workflowInputVariable)
                .waitForStatus(LHStatus.COMPLETED)
                .thenVerifyVariable(0, "my-obj", verifyVariableDoesNotContainFooKey)
                .start();
    }

    @Test
    void shouldIgnoreJsonKeyToRemoveWhenIsNotPresent() {
        Arg workflowInputVariable = Arg.of("my-obj", Map.of("baz", 2));
        Consumer<VariableValue> verifyVariableOnlyContainsBazJsonKey = variableValue -> {
            try {
                @SuppressWarnings("unchecked")
                Map<String, String> jsonMap = LHLibUtil.deserializeFromjson(variableValue.getJsonObj(), Map.class);
                assertThat(jsonMap).containsOnlyKeys("baz");
            } catch (LHJsonProcessingException e) {
                throw new RuntimeException(e);
            }
        };
        workflowVerifier
                .prepareRun(workflow, workflowInputVariable)
                .waitForStatus(LHStatus.COMPLETED)
                .thenVerifyVariable(0, "my-obj", verifyVariableOnlyContainsBazJsonKey)
                .start();
    }

    @LHWorkflow("json-mutation-workflow")
    public Workflow buildWorkflow() {
        return new WorkflowImpl("json-mutation-workflow", thread -> {
            WfRunVariable myObj = thread.addVariable("my-obj", VariableType.JSON_OBJ);

            thread.execute("ae-simple");
            thread.mutate(myObj, Operation.REMOVE_KEY, "foo");
        });
    }

    @LHTaskMethod("ae-simple")
    public String simpleTask() {
        return "hello there";
    }
}
