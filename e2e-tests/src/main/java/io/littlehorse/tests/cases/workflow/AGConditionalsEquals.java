package io.littlehorse.tests.cases.workflow;

import io.littlehorse.sdk.client.LHClient;
import io.littlehorse.sdk.common.config.LHWorkerConfig;
import io.littlehorse.sdk.common.exception.LHApiError;
import io.littlehorse.sdk.common.proto.Comparator;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.util.Arg;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.tests.TestFailure;
import io.littlehorse.tests.WorkflowLogicTest;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class AGConditionalsEquals extends WorkflowLogicTest {

    public AGConditionalsEquals(LHClient client, LHWorkerConfig workerConfig) {
        super(client, workerConfig);
    }

    public String getDescription() {
        return "Tests the 'EQUALS' comparator for various inputs.";
    }

    public Workflow getWorkflowImpl() {
        return new WorkflowImpl(getWorkflowName(), thread -> {
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

    private String assertThatFails(LHClient client, Object lhs, Object rhs)
            throws TestFailure, InterruptedException, LHApiError {
        String wfRunId = runWf(client, Arg.of("input", new InputObj(lhs, rhs)));
        Thread.sleep(100);
        assertStatus(client, wfRunId, LHStatus.ERROR);
        return wfRunId;
    }

    public List<Object> getTaskWorkerObjects() {
        return Arrays.asList(new AGSimpleTask());
    }

    public List<String> launchAndCheckWorkflows(LHClient client) throws TestFailure, InterruptedException, LHApiError {
        return Arrays.asList(
                runWithInputsAndCheckPath(client, Map.of("lhs", 1, "rhs", 2), true, false),
                runWithInputsAndCheckPath(client, Map.of("lhs", 2, "rhs", 2), true, true),
                runWithInputsAndCheckPath(client, Map.of("lhs", "hi", "rhs", "hi"), true, true),
                runWithInputsAndCheckPath(client, Map.of("lhs", 1.0, "rhs", 2.0), true, false),
                assertThatFails(client, Map.of("a", 1), Map.of("a", 1)),
                assertThatFails(client, Arrays.asList(0), Arrays.asList(0)),
                assertThatFails(client, false, "false"),
                assertThatFails(client, 1, 1.0),
                assertThatFails(client, 1, "one"),
                assertThatFails(client, 2, "2"),
                assertThatFails(client, 2, Map.of("a", 1)));
    }
}

class InputObj {

    public Object lhs;
    public Object rhs;

    public InputObj() {}

    public InputObj(Object lhs, Object rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }
}

class AGSimpleTask {

    @LHTaskMethod("ag-one")
    public boolean one() {
        return true;
    }

    @LHTaskMethod("ag-two")
    public boolean two() {
        return false;
    }
}
