package io.littlehorse.tests.cases.workflow;

import io.littlehorse.sdk.client.LHClient;
import io.littlehorse.sdk.common.config.LHWorkerConfig;
import io.littlehorse.sdk.common.exception.LHApiError;
import io.littlehorse.sdk.common.proto.Comparator;
import io.littlehorse.sdk.common.proto.VariableMutationType;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.tests.TestFailure;
import io.littlehorse.tests.WorkflowLogicTest;
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
                    WfRunVariable input = thread.addVariable("input", VariableType.INT);

                    /*
                    while (input > 0) {
                        execute(input);
                        input--
                    }
                     */
                    thread.doWhile(
                            thread.condition(input, Comparator.GREATER_THAN, 0),
                            whileBlock -> {
                                whileBlock.execute("aq-task", input);
                                whileBlock.mutate(input, VariableMutationType.SUBTRACT, 1);
                            });
                });
    }

    public List<Object> getTaskWorkerObjects() {
        return Arrays.asList(new AXSimpleTask());
    }

    public List<String> launchAndCheckWorkflows(LHClient client)
            throws TestFailure, InterruptedException, LHApiError {
        return Arrays.asList(
                runWithInputsAndCheckPath(client, 3, 3, 2, 1),
                runWithInputsAndCheckPath(client, 2, 2, 1),
                runWithInputsAndCheckPath(client, 0));
    }
}

class AXSimpleTask {

    @LHTaskMethod("aq-task")
    public int theTask(int input) {
        return input;
    }
}
