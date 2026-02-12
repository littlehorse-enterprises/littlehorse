package e2e;

import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.Operation;
import io.littlehorse.sdk.wfsdk.NodeOutput;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.LHWorkflow;
import io.littlehorse.test.WorkflowVerifier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@LHTest
public class PassingNodeOutputTest {
    private WorkflowVerifier verifier;

    @LHWorkflow("node-output-test")
    private Workflow nodeOutputTest;

    @Test
    void passesLatestInstanceOfNodeRun() {
        verifier.prepareRun(nodeOutputTest)
                .waitForStatus(LHStatus.COMPLETED)
                .thenVerifyTaskRun(0, 3, taskRun -> {
                    Assertions.assertEquals(taskRun.getTaskDefId().getName(), "node-output-negative");
                    Assertions.assertEquals(taskRun.getAttempts(0).getOutput().getInt(), -3);
                })
                .thenVerifyTaskRun(0, 7, taskRun -> {
                    Assertions.assertEquals(taskRun.getTaskDefId().getName(), "node-output-negative");
                    Assertions.assertEquals(taskRun.getAttempts(0).getOutput().getInt(), -2);
                })
                .thenVerifyTaskRun(0, 11, taskRun -> {
                    Assertions.assertEquals(taskRun.getTaskDefId().getName(), "node-output-negative");
                    Assertions.assertEquals(taskRun.getAttempts(0).getOutput().getInt(), -1);
                })
                .start();
    }

    @LHWorkflow("node-output-test")
    public Workflow getNodeOutputTest() {
        return Workflow.newWorkflow("node-output-test", wf -> {
            WfRunVariable input = wf.addVariable("num-iters", 3);

            wf.doWhile(wf.condition(input, Operation.GREATER_THAN, 0), loop -> {
                NodeOutput first = loop.execute("node-output-echo", input);
                loop.execute("node-output-negative", first);
                loop.mutate(input, Operation.SUBTRACT, 1);
            });
        });
    }

    @LHTaskMethod("node-output-echo")
    public int echo(int input) {
        return input;
    }

    @LHTaskMethod("node-output-negative")
    public int negate(int input) {
        return -1 * input;
    }
}
