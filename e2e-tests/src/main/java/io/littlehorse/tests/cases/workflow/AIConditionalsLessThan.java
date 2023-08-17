package io.littlehorse.tests.cases.workflow;

import io.littlehorse.sdk.client.LHClient;
import io.littlehorse.sdk.common.config.LHWorkerConfig;
import io.littlehorse.sdk.common.exception.LHApiError;
import io.littlehorse.sdk.common.proto.Comparator;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.tests.TestFailure;
import io.littlehorse.tests.WorkflowLogicTest;
import java.util.Arrays;
import java.util.List;

public class AIConditionalsLessThan extends WorkflowLogicTest {

    public AIConditionalsLessThan(LHClient client, LHWorkerConfig workerConfig) {
        super(client, workerConfig);
    }

    public String getDescription() {
        return "Tests LESS_THAN Comparator with various inputs";
    }

    public Workflow getWorkflowImpl() {
        return new WorkflowImpl(
            getWorkflowName(),
            thread -> {
                // Use an input JSON blob with two fields, LHS and RHS.
                // This allows us to test with various types on the left and the
                // right, since right now the JSON_OBJ var type does not have a
                // schema.
                WfRunVariable input = thread.addVariable(
                    "input",
                    VariableType.JSON_OBJ
                );

                // So that the run request succeeds even on workflows where we want
                // a crash.
                thread.execute("ai-one");

                thread.doIfElse(
                    thread.condition(
                        input.jsonPath("$.lhs"),
                        Comparator.LESS_THAN,
                        input.jsonPath("$.rhs")
                    ),
                    ifBlock -> {
                        ifBlock.execute("ai-one");
                    },
                    elseBlock -> {
                        elseBlock.execute("ai-two");
                    }
                );
            }
        );
    }

    private String runWithInputsAndCheck(
        LHClient client,
        Object lhs,
        Object rhs,
        boolean shouldEqual
    ) throws TestFailure, InterruptedException, LHApiError {
        AIInputObj input = new AIInputObj(lhs, rhs);

        if (shouldEqual) {
            return runWithInputsAndCheckPath(client, input, true, true);
        } else {
            return runWithInputsAndCheckPath(client, input, true, false);
        }
    }

    public List<Object> getTaskWorkerObjects() {
        return Arrays.asList(new AISimpleTask());
    }

    // private String twoInts() throws TestFailure

    public List<String> launchAndCheckWorkflows(LHClient client)
        throws TestFailure, InterruptedException, LHApiError {
        return Arrays.asList(
            runWithInputsAndCheck(client, 1, 2, true),
            runWithInputsAndCheck(client, 1, 1, false),
            runWithInputsAndCheck(client, "hi", "hi", false),
            runWithInputsAndCheck(client, "a", "b", true),
            runWithInputsAndCheck(client, 1.0, 1.0, false),
            runWithInputsAndCheck(client, 5, 4, false)
        );
    }
}

class AIInputObj {

    public Object lhs;
    public Object rhs;

    public AIInputObj() {}

    public AIInputObj(Object lhs, Object rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }
}

class AISimpleTask {

    @LHTaskMethod("ai-one")
    public boolean one() {
        return true;
    }

    @LHTaskMethod("ai-two")
    public boolean two() {
        return false;
    }
}
