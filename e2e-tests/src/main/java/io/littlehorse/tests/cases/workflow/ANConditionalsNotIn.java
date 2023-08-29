package io.littlehorse.tests.cases.workflow;

import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.Comparator;
import io.littlehorse.sdk.common.proto.LHPublicApiGrpc.LHPublicApiBlockingStub;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.util.Arg;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.tests.TestFailure;
import io.littlehorse.tests.WorkflowLogicTest;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ANConditionalsNotIn extends WorkflowLogicTest {

    public ANConditionalsNotIn(LHPublicApiBlockingStub client, LHConfig workerConfig) {
        super(client, workerConfig);
    }

    public String getDescription() {
        return "Tests NOT_IN Comparator with various inputs";
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
            thread.execute("an-one");

            thread.doIfElse(
                    thread.condition(input.jsonPath("$.lhs"), Comparator.NOT_IN, input.jsonPath("$.rhs")),
                    ifBlock -> {
                        ifBlock.execute("an-one");
                    },
                    elseBlock -> {
                        elseBlock.execute("an-two");
                    });
        });
    }

    private String assertThatFails(LHPublicApiBlockingStub client, Object lhs, Object rhs)
            throws TestFailure, InterruptedException, IOException {
        String wfRunId = runWf(client, Arg.of("input", new ANInputObj(lhs, rhs)));
        Thread.sleep(3000);
        assertStatus(client, wfRunId, LHStatus.ERROR);
        return wfRunId;
    }

    public List<Object> getTaskWorkerObjects() {
        return Arrays.asList(new ANSimpleTask());
    }

    // private String twoInts() throws TestFailure

    public List<String> launchAndCheckWorkflows(LHPublicApiBlockingStub client)
            throws TestFailure, InterruptedException, IOException {
        return Arrays.asList(
                runWithInputsAndCheckPath(client, new ANInputObj(Map.of("a", 1), Map.of("a", 1)), true, true),
                runWithInputsAndCheckPath(client, new ANInputObj("hi", Map.of("hi", 2)), true, false),
                runWithInputsAndCheckPath(client, new ANInputObj(2, Map.of("hi", 2)), true, true),
                runWithInputsAndCheckPath(client, new ANInputObj(Arrays.asList(0), Arrays.asList(0)), true, true),
                runWithInputsAndCheckPath(client, new ANInputObj(0, Arrays.asList(0)), true, false),
                runWithInputsAndCheckPath(client, new ANInputObj(1, "one"), true, true),
                runWithInputsAndCheckPath(client, new ANInputObj("o", "one"), true, false),
                runWithInputsAndCheckPath(client, new ANInputObj(2, "2"), true, false),
                assertThatFails(client, 1, 1.0),
                runWithInputsAndCheckPath(client, new ANInputObj(2, Map.of("a", 1)), true, true));
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
