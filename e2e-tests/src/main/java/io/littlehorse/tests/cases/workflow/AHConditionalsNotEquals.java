package io.littlehorse.tests.cases.workflow;

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
import io.littlehorse.tests.TestFailure;
import io.littlehorse.tests.WorkflowLogicTest;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class AHConditionalsNotEquals extends WorkflowLogicTest {

    public AHConditionalsNotEquals(LHClient client, LHWorkerConfig workerConfig) {
        super(client, workerConfig);
    }

    public String getDescription() {
        return "Tests the 'NOT_EQUALS' comparator with various inputs.";
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
                thread.execute("ah-one");

                thread.doIfElse(
                    thread.condition(
                        input.jsonPath("$.lhs"),
                        ComparatorPb.NOT_EQUALS,
                        input.jsonPath("$.rhs")
                    ),
                    ifBlock -> {
                        ifBlock.execute("ah-one");
                    },
                    elseBlock -> {
                        elseBlock.execute("ah-two");
                    }
                );
            }
        );
    }

    public List<Object> getTaskWorkerObjects() {
        return Arrays.asList(new AHSimpleTask());
    }

    private String assertThatFails(LHClient client, Object lhs, Object rhs)
        throws TestFailure, InterruptedException, LHApiError {
        String wfRunId = runWf(client, Arg.of("input", new AHInputObj(lhs, rhs)));
        Thread.sleep(100);
        assertStatus(client, wfRunId, LHStatusPb.ERROR);
        return wfRunId;
    }

    public List<String> launchAndCheckWorkflows(LHClient client)
        throws TestFailure, InterruptedException, LHApiError {
        return Arrays.asList(
            runWithInputsAndCheckPath(client, new AHInputObj(1, 2), true, true),
            runWithInputsAndCheckPath(client, new AHInputObj(1, 1), true, false),
            runWithInputsAndCheckPath(
                client,
                new AHInputObj("hi", "hi"),
                true,
                false
            ),
            runWithInputsAndCheckPath(client, new AHInputObj(1.0, 1.0), true, false),
            assertThatFails(client, Map.of("a", 1), Map.of("a", 1)),
            assertThatFails(client, Arrays.asList(0), Arrays.asList(0)),
            assertThatFails(client, false, "false"),
            assertThatFails(client, 1, 1.0),
            assertThatFails(client, 1, "one"),
            assertThatFails(client, 2, "2"),
            assertThatFails(client, 2, Map.of("a", 1))
        );
    }
}

class AHInputObj {

    public Object lhs;
    public Object rhs;

    public AHInputObj() {}

    public AHInputObj(Object lhs, Object rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }
}

class AHSimpleTask {

    @LHTaskMethod("ah-one")
    public boolean one() {
        return true;
    }

    @LHTaskMethod("ah-two")
    public boolean two() {
        return false;
    }
}
