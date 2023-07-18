package io.littlehorse.tests.cases.workflow;

import io.littlehorse.sdk.client.LHClient;
import io.littlehorse.sdk.common.config.LHWorkerConfig;
import io.littlehorse.sdk.common.exception.LHApiError;
import io.littlehorse.sdk.common.proto.ComparatorPb;
import io.littlehorse.sdk.common.proto.VariableTypePb;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.tests.TestFailure;
import io.littlehorse.tests.WorkflowLogicTest;
import java.util.Arrays;
import java.util.List;

public class AKConditionalsGreaterThan extends WorkflowLogicTest {

    public AKConditionalsGreaterThan(LHClient client, LHWorkerConfig workerConfig) {
        super(client, workerConfig);
    }

    public String getDescription() {
        return "Tests GREATER_THAN Comparator with various inputs";
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
                    VariableTypePb.JSON_OBJ
                );

                // So that the run request succeeds even on workflows where we want
                // a crash.
                thread.execute("ak-one");

                thread.doIfElse(
                    thread.condition(
                        input.jsonPath("$.lhs"),
                        ComparatorPb.GREATER_THAN,
                        input.jsonPath("$.rhs")
                    ),
                    ifBlock -> {
                        ifBlock.execute("ak-one");
                    },
                    elseBlock -> {
                        elseBlock.execute("ak-two");
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
        InputObj input = new InputObj(lhs, rhs);

        if (shouldEqual) {
            return runWithInputsAndCheckPath(client, input, true, true);
        } else {
            return runWithInputsAndCheckPath(client, input, true, false);
        }
    }

    public List<Object> getTaskWorkerObjects() {
        return Arrays.asList(new AKSimpleTask());
    }

    // private String twoInts() throws TestFailure

    public List<String> launchAndCheckWorkflows(LHClient client)
        throws TestFailure, InterruptedException, LHApiError {
        return Arrays.asList(
            runWithInputsAndCheck(client, 1, 2, false),
            runWithInputsAndCheck(client, 1, 1, false),
            runWithInputsAndCheck(client, 2, 1, true),
            runWithInputsAndCheck(client, "hi", "hi", false),
            runWithInputsAndCheck(client, "a", "b", false),
            runWithInputsAndCheck(client, 5.4, 4.0, true)
        );
    }
}

class AKInputObj {

    public Object lhs;
    public Object rhs;

    public AKInputObj() {}

    public AKInputObj(Object lhs, Object rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }
}

class AKSimpleTask {

    @LHTaskMethod("ak-one")
    public boolean one() {
        return true;
    }

    @LHTaskMethod("ak-two")
    public boolean two() {
        return false;
    }
}
