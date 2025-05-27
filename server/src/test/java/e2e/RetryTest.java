package e2e;

import static org.assertj.core.api.Assertions.assertThat;

import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.proto.ExponentialBackoffRetryPolicy;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.TaskAttempt;
import io.littlehorse.sdk.common.proto.TaskStatus;
import io.littlehorse.sdk.common.util.Arg;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.sdk.worker.WorkerContext;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.LHWorkflow;
import io.littlehorse.test.WorkflowVerifier;
import java.time.Duration;

import org.junit.jupiter.api.Test;

@LHTest
public class RetryTest {

    private WorkflowVerifier verifier;

    @LHWorkflow("retry-test")
    private Workflow retryTest;

    @LHWorkflow("retry-test")
    public Workflow getRetryTestWf() {
        return Workflow.newWorkflow("retry-test", wf -> {
            WfRunVariable timesToFailSimple = wf.addVariable("times-to-fail-simple", 1);
            WfRunVariable timesToFailBackoff = wf.addVariable("times-to-fail-backoff", 1);

            wf.execute("retry-task", timesToFailSimple).withRetries(2);
            wf.execute("retry-task", timesToFailBackoff)
                    .withRetries(2)
                    .withExponentialBackoff(ExponentialBackoffRetryPolicy.newBuilder()
                            .setBaseIntervalMs(1000)
                            .setMultiplier(2)
                            .setMaxDelayMs(2000)
                            .build());
        });
    }

    @Test
    void simpleRetriesShouldBeImmediate() {
        verifier.prepareRun(retryTest, Arg.of("times-to-fail-simple", 1), Arg.of("times-to-fail-backoff", 0))
                .waitForNodeRunStatus(0, 1, LHStatus.COMPLETED)
                .thenVerifyTaskRun(0, 1, task -> {
                    assertThat(task.getAttemptsCount()).isEqualTo(2);

                    // The "endTime" is the time that the `ReportTaskRun` was pushed to Kafka. The `scheduleTime` of the
                    // next TaskRun should be the time that the `ReportTaskRun` is processed by the command processor.
                    // That generally happens in 10-30ms, so we will put a time limit of 50. If this becomes flaky, we
                    // can re-evaluate it.
                    assertThat(millisecondsBetween(task.getAttempts(0), task.getAttempts(1)))
                            .isLessThan(500);
                })
                .waitForStatus(LHStatus.COMPLETED)
                .start();
    }

    @Test
    void simpleRetriesShouldBeExhausted() {
        verifier.prepareRun(retryTest, Arg.of("times-to-fail-simple", 3), Arg.of("times-to-fail-backoff", 0))
                .waitForNodeRunStatus(0, 1, LHStatus.ERROR)
                .thenVerifyTaskRun(0, 1, task -> {
                    assertThat(task.getAttemptsCount()).isEqualTo(3);
                })
                .waitForStatus(LHStatus.ERROR)
                .start();
    }

    @Test
    void exponentialRetriesShouldNotBeImmediate() {
        verifier.prepareRun(retryTest, Arg.of("times-to-fail-simple", 0), Arg.of("times-to-fail-backoff", 2))
                .waitForNodeRunStatus(0, 2, LHStatus.COMPLETED, Duration.ofSeconds(5))
                .thenVerifyTaskRun(0, 2, task -> {
                    assertThat(task.getAttemptsCount()).isEqualTo(3);

                    // The base is 1000, then the max delay is 2000.
                    assertThat(millisecondsBetween(task.getAttempts(0), task.getAttempts(1)))
                            .isBetween(1000L, 2100L);
                    assertThat(millisecondsBetween(task.getAttempts(1), task.getAttempts(2)))
                            .isBetween(2000L, 3100L);
                })
                .waitForStatus(LHStatus.COMPLETED)
                .start();
    }

    @Test
    void exponentialRetriesShouldExhaust() {
        verifier.prepareRun(retryTest, Arg.of("times-to-fail-simple", 0), Arg.of("times-to-fail-backoff", 3))
                .waitForNodeRunStatus(0, 2, LHStatus.ERROR, Duration.ofSeconds(5))
                .thenVerifyTaskRun(0, 2, task -> {
                    assertThat(task.getAttemptsCount()).isEqualTo(3);
                    assertThat(task.getStatus()).isEqualTo(TaskStatus.TASK_FAILED);
                })
                .waitForStatus(LHStatus.ERROR)
                .start();
    }

    @LHTaskMethod("retry-task")
    public void retryTask(int numberOfTimesToFail, WorkerContext context) {
        if (numberOfTimesToFail > context.getAttemptNumber()) {
            throw new RuntimeException("Failing! Woohoo!");
        }
    }

    private long millisecondsBetween(TaskAttempt first, TaskAttempt second) {
        long firstEndTime = LHLibUtil.fromProtoTs(first.getEndTime()).getTime();
        long secondScheduleTime =
                LHLibUtil.fromProtoTs(second.getScheduleTime()).getTime();
        return secondScheduleTime - firstEndTime;
    }
}
