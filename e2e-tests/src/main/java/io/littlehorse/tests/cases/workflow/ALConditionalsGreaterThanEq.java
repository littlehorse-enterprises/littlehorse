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
import io.littlehorse.tests.LogicTestFailure;
import io.littlehorse.tests.WorkflowLogicTest;
import java.util.Arrays;
import java.util.List;

public class ALConditionalsGreaterThanEq extends WorkflowLogicTest {

    public ALConditionalsGreaterThanEq(LHClient client, LHWorkerConfig workerConfig) {
        super(client, workerConfig);
    }

    public String getDescription() {
        return "Tests GREATER_THAN_EQ Comparator with various inputs";
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
                thread.execute("al-one");

                thread.doIfElse(
                    thread.condition(
                        input.jsonPath("$.lhs"),
                        ComparatorPb.GREATER_THAN_EQ,
                        input.jsonPath("$.rhs")
                    ),
                    ifBlock -> {
                        ifBlock.execute("al-one");
                    },
                    elseBlock -> {
                        elseBlock.execute("al-two");
                    }
                );
            }
        );
    }

    public List<Object> getTaskWorkerObjects() {
        return Arrays.asList(new ALSimpleTask());
    }

    public List<String> launchAndCheckWorkflows(LHClient client)
        throws LogicTestFailure, InterruptedException, LHApiError {
        return Arrays.asList(
            runWithInputsAndCheckPath(client, new ALInputObj(1, 2), true, false),
            runWithInputsAndCheckPath(client, new ALInputObj(1, 1), true, true),
            runWithInputsAndCheckPath(client, new ALInputObj("hi", "hi"), true, true),
            runWithInputsAndCheckPath(client, new ALInputObj("a", "b"), true, false),
            runWithInputsAndCheckPath(client, new ALInputObj(1.0, 1.0), true, true),
            runWithInputsAndCheckPath(client, new ALInputObj(5, 4), true, true)
        );
    }
}

class ALInputObj {

    public Object lhs;
    public Object rhs;

    public ALInputObj() {}

    public ALInputObj(Object lhs, Object rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }
}

class ALSimpleTask {

    @LHTaskMethod("al-one")
    public boolean one() {
        return true;
    }

    @LHTaskMethod("al-two")
    public boolean two() {
        return false;
    }
}
