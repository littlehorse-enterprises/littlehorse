package e2e;

import static org.assertj.core.api.Assertions.assertThat;

import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.TaskStatus;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.util.Arg;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.LHWorkflow;
import io.littlehorse.test.WorkflowVerifier;
import java.time.Duration;
import org.junit.jupiter.api.Test;

/**
 * Covers the case (reproduced by the modified `example-basic`) where a task worker returns a byte
 * array whose serialized size exceeds the server's max record size (LHS_PRODUCER_MAX_REQUEST_SIZE,
 * default 1047000 bytes).
 *
 * <p>When that happens, the server cannot produce the {@code ReportTaskRun} command to Kafka and
 * answers the worker with {@code RESOURCE_EXHAUSTED} ("Record too large"). The worker exhausts its
 * report retries and gives up, so the {@code TaskRun} stays {@code TASK_RUNNING} and the WfRun never
 * advances past that node.
 */
@LHTest
public class TaskOutputTooLargeTest {

    // The default producer max request size (see LHServerConfig#getProducerMaxRequestSize()).
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
    void shouldCompleteWhenTaskOutputFitsWithinMaxRecordSize() {
        verifier.prepareRun(taskOutputSizeWf, Arg.of("payload-size", 1024))
                .waitForStatus(LHStatus.COMPLETED)
                .thenVerifyTaskRun(
                        0, 1, taskRun -> assertThat(taskRun.getStatus()).isEqualTo(TaskStatus.TASK_SUCCESS))
                .start();
    }

    @Test
    void shouldNotAdvanceWhenTaskOutputExceedsMaxRecordSize() {
        verifier.prepareRun(taskOutputSizeWf, Arg.of("payload-size", TWO_MIB))
                // The task gets claimed and executed, so it reaches TASK_RUNNING on the server...
                .waitForTaskStatus(0, 1, TaskStatus.TASK_RUNNING)
                // ...but the oversized output can never be reported, so the WfRun stays RUNNING and
                // the TaskRun never reaches a terminal status.
                .waitForStatus(LHStatus.RUNNING, Duration.ofSeconds(3))
                .thenVerifyTaskRun(
                        0, 1, taskRun -> assertThat(taskRun.getStatus()).isEqualTo(TaskStatus.TASK_RUNNING))
                .start();
    }

    @LHTaskMethod("return-bytes-of-size")
    public byte[] returnBytesOfSize(int size) {
        return new byte[size];
    }
}

