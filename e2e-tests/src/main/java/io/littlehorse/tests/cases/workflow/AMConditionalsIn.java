package io.littlehorse.tests.cases.workflow;

import io.littlehorse.sdk.common.config.LHWorkerConfig;
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

public class AMConditionalsIn extends WorkflowLogicTest {

    public AMConditionalsIn(LHPublicApiBlockingStub client, LHWorkerConfig workerConfig) {
        super(client, workerConfig);
    }

    public String getDescription() {
        return "Tests IN Comparator with various inputs";
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
            thread.execute("am-one");

            thread.doIfElse(
                    thread.condition(input.jsonPath("$.lhs"), Comparator.IN, input.jsonPath("$.rhs")),
                    ifBlock -> {
                        ifBlock.execute("am-one");
                    },
                    elseBlock -> {
                        elseBlock.execute("am-two");
                    });
        });
    }

    private String assertThatFails(LHPublicApiBlockingStub client, Object lhs, Object rhs)
            throws TestFailure, InterruptedException, IOException {
        String wfRunId = runWf(client, Arg.of("input", new AMInputObj(lhs, rhs)));
        Thread.sleep(200);
        assertStatus(client, wfRunId, LHStatus.ERROR);
        return wfRunId;
    }

    private String runWithInputsAndCheck(LHPublicApiBlockingStub client, Object lhs, Object rhs, boolean shouldEqual)
            throws TestFailure, InterruptedException, IOException {
        AMInputObj input = new AMInputObj(lhs, rhs);

        if (shouldEqual) {
            return runWithInputsAndCheckPath(client, input, true, true);
        } else {
            return runWithInputsAndCheckPath(client, input, true, false);
        }
    }

    public List<Object> getTaskWorkerObjects() {
        return Arrays.asList(new AMSimpleTask());
    }

    public List<String> launchAndCheckWorkflows(LHPublicApiBlockingStub client)
            throws TestFailure, InterruptedException, IOException {
        return Arrays.asList(
                runWithInputsAndCheck(client, Map.of("a", 1), Map.of("a", 1), false),
                runWithInputsAndCheck(client, "hi", Map.of("hi", 2), true),
                runWithInputsAndCheck(client, 2, Map.of("hi", 2), false),
                // Will check for '[0]'
                runWithInputsAndCheck(client, Arrays.asList(0), Arrays.asList(0), false),
                runWithInputsAndCheck(client, 0, Arrays.asList(0), true),
                assertThatFails(client, 1, 1.0),
                runWithInputsAndCheck(client, 1, "one", false),
                runWithInputsAndCheck(client, "o", "one", true),
                runWithInputsAndCheck(client, 2, "2", true),
                runWithInputsAndCheck(client, 2, Map.of("a", 1), false));
    }
}

class AMInputObj {

    public Object lhs;
    public Object rhs;

    public AMInputObj() {}

    public AMInputObj(Object lhs, Object rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }
}

class AMSimpleTask {

    @LHTaskMethod("am-one")
    public boolean one() {
        return true;
    }

    @LHTaskMethod("am-two")
    public boolean two() {
        return false;
    }
}
