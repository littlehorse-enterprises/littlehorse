package io.littlehorse.jtests.test.workflowtests;

import io.littlehorse.jtests.test.LogicTestFailure;
import io.littlehorse.jtests.test.WorkflowLogicTest;
import io.littlehorse.sdk.client.LHClient;
import io.littlehorse.sdk.common.config.LHWorkerConfig;
import io.littlehorse.sdk.common.exception.LHApiError;
import io.littlehorse.sdk.common.proto.ComparatorPb;
import io.littlehorse.sdk.common.proto.LHStatusPb;
import io.littlehorse.sdk.common.proto.VariableTypePb;
import io.littlehorse.sdk.common.util.Arg;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskMethod;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ANConditionalsNotIn extends WorkflowLogicTest {

    public ANConditionalsNotIn(LHClient client, LHWorkerConfig workerConfig) {
        super(client, workerConfig);
    }

    public String getDescription() {
        return "Tests NOT_IN Comparator with various inputs";
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
                thread.execute("an-one");

                thread.doIfElse(
                    thread.condition(
                        input.jsonPath("$.lhs"),
                        ComparatorPb.NOT_IN,
                        input.jsonPath("$.rhs")
                    ),
                    ifBlock -> {
                        ifBlock.execute("an-one");
                    },
                    elseBlock -> {
                        elseBlock.execute("an-two");
                    }
                );
            }
        );
    }

    private String assertThatFails(LHClient client, Object lhs, Object rhs)
        throws LogicTestFailure, InterruptedException, LHApiError {
        String wfRunId = runWf(client, Arg.of("input", new ANInputObj(lhs, rhs)));
        Thread.sleep(3000);
        assertStatus(client, wfRunId, LHStatusPb.ERROR);
        return wfRunId;
    }

    public List<Object> getTaskWorkerObjects() {
        return Arrays.asList(new ANSimpleTask());
    }

    // private String twoInts() throws TestFailure

    public List<String> launchAndCheckWorkflows(LHClient client)
        throws LogicTestFailure, InterruptedException, LHApiError {
        return Arrays.asList(
            runWithInputsAndCheckPath(
                client,
                new ANInputObj(Map.of("a", 1), Map.of("a", 1)),
                true,
                true
            ),
            runWithInputsAndCheckPath(
                client,
                new ANInputObj("hi", Map.of("hi", 2)),
                true,
                false
            ),
            runWithInputsAndCheckPath(
                client,
                new ANInputObj(2, Map.of("hi", 2)),
                true,
                true
            ),
            runWithInputsAndCheckPath(
                client,
                new ANInputObj(Arrays.asList(0), Arrays.asList(0)),
                true,
                true
            ),
            runWithInputsAndCheckPath(
                client,
                new ANInputObj(0, Arrays.asList(0)),
                true,
                false
            ),
            runWithInputsAndCheckPath(client, new ANInputObj(1, "one"), true, true),
            runWithInputsAndCheckPath(
                client,
                new ANInputObj("o", "one"),
                true,
                false
            ),
            runWithInputsAndCheckPath(client, new ANInputObj(2, "2"), true, false),
            assertThatFails(client, 1, 1.0),
            runWithInputsAndCheckPath(
                client,
                new ANInputObj(2, Map.of("a", 1)),
                true,
                true
            )
        );
    }
}

class ANInputObj {

    public Object lhs;
    public Object rhs;

    public ANInputObj() {}

    public ANInputObj(Object lhs, Object rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }
}

class ANSimpleTask {

    @LHTaskMethod("an-one")
    public boolean one() {
        return true;
    }

    @LHTaskMethod("an-two")
    public boolean two() {
        return false;
    }
}
