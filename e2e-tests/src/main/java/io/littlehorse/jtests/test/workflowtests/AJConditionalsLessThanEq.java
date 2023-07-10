package io.littlehorse.jtests.test.workflowtests;

import io.littlehorse.jtests.test.LogicTestFailure;
import io.littlehorse.jtests.test.WorkflowLogicTest;
import io.littlehorse.sdk.client.LHClient;
import io.littlehorse.sdk.common.config.LHWorkerConfig;
import io.littlehorse.sdk.common.exception.LHApiError;
import io.littlehorse.sdk.common.proto.ComparatorPb;
import io.littlehorse.sdk.common.proto.VariableTypePb;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskMethod;
import java.util.Arrays;
import java.util.List;

public class AJConditionalsLessThanEq extends WorkflowLogicTest {

    public AJConditionalsLessThanEq(LHClient client, LHWorkerConfig workerConfig) {
        super(client, workerConfig);
    }

    public String getDescription() {
        return "Tests LESS_THAN_EQ Comparator with various inputs";
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
                thread.execute("aj-one");

                thread.doIfElse(
                    thread.condition(
                        input.jsonPath("$.lhs"),
                        ComparatorPb.LESS_THAN_EQ,
                        input.jsonPath("$.rhs")
                    ),
                    ifBlock -> {
                        ifBlock.execute("aj-one");
                    },
                    elseBlock -> {
                        elseBlock.execute("aj-two");
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
    ) throws LogicTestFailure, InterruptedException, LHApiError {
        InputObj input = new InputObj(lhs, rhs);

        if (shouldEqual) {
            return runWithInputsAndCheckPath(client, input, true, true);
        } else {
            return runWithInputsAndCheckPath(client, input, true, false);
        }
    }

    public List<Object> getTaskWorkerObjects() {
        return Arrays.asList(new AJSimpleTask());
    }

    // private String twoInts() throws TestFailure

    public List<String> launchAndCheckWorkflows(LHClient client)
        throws LogicTestFailure, InterruptedException, LHApiError {
        return Arrays.asList(
            runWithInputsAndCheck(client, 1, 2, true),
            runWithInputsAndCheck(client, 1, 1, true),
            runWithInputsAndCheck(client, "hi", "hi", true),
            runWithInputsAndCheck(client, "a", "b", true),
            runWithInputsAndCheck(client, 1.0, 1.0, true),
            runWithInputsAndCheck(client, 5, 4, false)
        );
    }
}

class AJInputObj {

    public Object lhs;
    public Object rhs;

    public AJInputObj() {}

    public AJInputObj(Object lhs, Object rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }
}

class AJSimpleTask {

    @LHTaskMethod("aj-one")
    public boolean one() {
        return true;
    }

    @LHTaskMethod("aj-two")
    public boolean two() {
        return false;
    }
}
