package io.littlehorse.jtests.test.workflowtests;

import io.littlehorse.jtests.test.LogicTestFailure;
import io.littlehorse.jtests.test.WorkflowLogicTest;
import io.littlehorse.sdk.client.LHClient;
import io.littlehorse.sdk.common.config.LHWorkerConfig;
import io.littlehorse.sdk.common.exception.LHApiError;
import io.littlehorse.sdk.common.proto.ComparatorPb;
import io.littlehorse.sdk.common.proto.VariableMutationTypePb;
import io.littlehorse.sdk.common.proto.VariableTypePb;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskMethod;
import java.util.Arrays;
import java.util.List;

public class AXConditionalWhileLogic extends WorkflowLogicTest {

    public AXConditionalWhileLogic(LHClient client, LHWorkerConfig workerConfig) {
        super(client, workerConfig);
    }

    public String getDescription() {
        return "Test while conditional.";
    }

    public Workflow getWorkflowImpl() {
        return new WorkflowImpl(
            getWorkflowName(),
            thread -> {
                WfRunVariable input = thread.addVariable("input", VariableTypePb.INT);

                /*
                while (input > 0) {
                    execute(input);
                    input--
                }
                 */
                thread.doWhile(
                    thread.condition(input, ComparatorPb.GREATER_THAN, 0),
                    whileBlock -> {
                        whileBlock.execute("aq-task", input);
                        whileBlock.mutate(input, VariableMutationTypePb.SUBTRACT, 1);
                    }
                );
            }
        );
    }

    public List<Object> getTaskWorkerObjects() {
        return Arrays.asList(new AXSimpleTask());
    }

    public List<String> launchAndCheckWorkflows(LHClient client)
        throws LogicTestFailure, InterruptedException, LHApiError {
        return Arrays.asList(
            runWithInputsAndCheckPath(client, 3, 3, 2, 1),
            runWithInputsAndCheckPath(client, 4, 4, 3, 2, 1)
        );
    }
}

class AXSimpleTask {

    @LHTaskMethod("aq-task")
    public int theTask(int input) {
        return input;
    }
}
