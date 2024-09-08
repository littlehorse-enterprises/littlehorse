package e2e;

import io.littlehorse.sdk.common.proto.Comparator;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.VariableMutationType;
import io.littlehorse.sdk.common.util.Arg;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.LHWorkflow;
import io.littlehorse.test.WorkflowVerifier;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

@LHTest
public class WhileLoopTest {

    private WorkflowVerifier verifier;

    @LHWorkflow("while-loop-wf")
    public Workflow whileLoopWf;

    @Test
    void shouldNotExecuteLoopIfConditionIsFalse() {
        verifier.prepareRun(whileLoopWf, Arg.of("counter", -1))
                .thenVerifyWfRun(wfRun -> {
                    // The WfRun should have completed in one Command
                    Assertions.assertThat(wfRun.getStatus()).isEqualTo(LHStatus.COMPLETED);
                })
                .thenVerifyAllTaskRuns(taskRuns -> {
                    Assertions.assertThat(taskRuns.isEmpty());
                })
                .start();
    }

    @Test
    void shouldExecuteLoopThreeTimes() {
        // This also implicitly tests default variable values
        verifier.prepareRun(whileLoopWf)
                .waitForStatus(LHStatus.COMPLETED)
                .thenVerifyAllTaskRuns(taskRuns -> {
                    Assertions.assertThat(taskRuns.size()).isEqualTo(3);
                })
                .start();
    }

    @LHWorkflow("while-loop-wf")
    public Workflow getWorkflow() {
        return Workflow.newWorkflow("while-loop-wf", wf -> {
            WfRunVariable counter = wf.addVariable("counter", 3);
            wf.doWhile(wf.condition(counter, Comparator.GREATER_THAN, 0), loop -> {
                loop.execute("while-loop-obiwan");
                loop.mutate(counter, VariableMutationType.SUBTRACT, 1);
            });
        });
    }

    @LHTaskMethod("while-loop-obiwan")
    public void theTask() {
        System.out.println("hello there!");
    }
}
