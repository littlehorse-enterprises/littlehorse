package e2e;

import static org.assertj.core.api.Assertions.assertThat;

import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.TaskAttempt;
import io.littlehorse.sdk.common.proto.TaskStatus;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.sdk.common.util.Arg;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.LHWorkflow;
import io.littlehorse.test.WorkflowVerifier;
import org.junit.jupiter.api.Test;

@LHTest
public class TaskOutputTooLargeTest {

    // Bigger than the default producer max request size (see LHServerConfig#getProducerMaxRequestSize()).
    private static final int TWO_MIB = 2 * 1024 * 1024;

    private WorkflowVerifier verifier;

    @LHWorkflow("task-output-size")
    private Workflow taskOutputSizeWf;

    @LHWorkflow("task-output-size")
    public Workflow getTaskOutputSizeWf() {
        return Workflow.newWorkflow("task-output-size", wf -> {
            WfRunVariable payloadSize =
                    wf.addVariable("payload-size", VariableType.INT).required();
            wf.execute("return-bytes-of-size", payloadSize);
        });
    }

    @Test
    void shouldNotAdvanceWhenTaskOutputExceedsMaxRecordSize() {
        verifier.prepareRun(taskOutputSizeWf, Arg.of("payload-size", TWO_MIB))
                // The task gets claimed and executed, so it reaches TASK_RUNNING on the server...
                .waitForTaskStatus(0, 1, TaskStatus.TASK_OUTPUT_SERDE_ERROR)
                .waitForStatus(LHStatus.ERROR)
                .thenVerifyTaskRun(0, 1, taskRun -> {
                    assertThat(taskRun.getAttemptsList()).hasSize(1);
                    TaskAttempt taskAttempt = taskRun.getAttempts(0);
                    assertThat(taskAttempt.getOutput().getValueCase()).isEqualTo(VariableValue.ValueCase.VALUE_NOT_SET);
                    assertThat(taskAttempt.getStatus()).isEqualTo(TaskStatus.TASK_OUTPUT_SERDE_ERROR);
                    assertThat(taskAttempt.getError().getMessage()).contains("RESOURCE_EXHAUSTED");
                })
                .start();
    }

    @LHTaskMethod("return-bytes-of-size")
    public byte[] returnBytesOfSize(int size) {
        return new byte[size];
    }
}
