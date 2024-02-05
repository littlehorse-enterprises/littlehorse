package e2e;

import io.grpc.StatusRuntimeException;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.ThreadRetentionPolicy;
import io.littlehorse.sdk.common.proto.WfRunId;
import io.littlehorse.sdk.common.proto.WorkflowRetentionPolicy;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.LHWorkflow;
import io.littlehorse.test.WorkflowVerifier;
import io.littlehorse.test.exception.LHTestExceptionUtil;
import java.time.Duration;
import java.util.Map;
import java.util.function.Function;
import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

@LHTest(externalEventNames = {RetentionTest.RETENTION_PARENT_EVENT, RetentionTest.RETENTION_CHILD_EVENT})
public class RetentionTest {

    public static final String RETENTION_PARENT_EVENT = "retention-parent-event";
    public static final String RETENTION_CHILD_EVENT = "retention-child-event";

    private WorkflowVerifier verifier;
    private LittleHorseBlockingStub lhClient;

    @LHWorkflow("child-thread-retention-test")
    private Workflow threadRetentionWorkflow;

    @LHWorkflow("wf-run-retention-test")
    private Workflow wfRunRetentionWorkflow;

    @LHWorkflow("child-thread-retention-test")
    public Workflow buildRetentionWorkflow() {
        return Workflow.newWorkflow("retention-test", wf -> {
            wf.spawnThread(
                    child -> {
                        child.withRetentionPolicy(ThreadRetentionPolicy.newBuilder()
                                // To avoid sleeping in test framework, delete immediately
                                .setSecondsAfterThreadTermination(0)
                                .build());
                        child.waitForEvent(RETENTION_CHILD_EVENT);
                    },
                    "child",
                    null);

            // Wait twice so we have control and can inspect the WfRun twice.
            wf.waitForEvent(RETENTION_PARENT_EVENT);

            // Ensure that the threadRunNumber for this thread is 2, not 1
            wf.spawnThread(child -> {}, "second-child", null);
        });
    }

    @LHWorkflow("wf-run-retention-test")
    public Workflow buildWfRunRetentionWorkflow() {
        Workflow out = Workflow.newWorkflow("wf-run-retention-test", wf -> {});
        out.withRetentionPolicy(WorkflowRetentionPolicy.newBuilder()
                .setSecondsAfterWfTermination(1)
                .build());
        return out;
    }

    @Test
    void shouldDeleteWfRunAfterRetentionExpires() {
        WfRunId id = verifier.prepareRun(wfRunRetentionWorkflow)
                .waitForStatus(LHStatus.COMPLETED)
                .start();

        Awaitility.await().atMost(Duration.ofSeconds(4)).until(() -> {
            return isNotFound((client) -> {
                client.getWfRun(id);
                return true;
            });
        });
    }

    private boolean isNotFound(Function<LittleHorseBlockingStub, Boolean> callable) {
        StatusRuntimeException caught = null;
        try {
            callable.apply(lhClient);
        } catch (StatusRuntimeException exn) {
            if (LHTestExceptionUtil.isNotFoundException(exn)) {
                caught = exn;
            }
        }
        return caught != null;
    }

    @Test
    void childThreadShouldGcImmediately() {
        verifier.prepareRun(threadRetentionWorkflow)
                .thenVerifyWfRun(wfRun -> {
                    Assertions.assertThat(wfRun.getThreadRunsCount()).isEqualTo(2);
                })
                .thenSendExternalEventJsonContent(RETENTION_CHILD_EVENT, Map.of())
                .thenVerifyWfRun(wfRun -> {
                    Assertions.assertThat(wfRun.getThreadRunsCount()).isEqualTo(1);
                })
                .thenSendExternalEventJsonContent(RETENTION_PARENT_EVENT, Map.of())
                .thenVerifyWfRun(wfRun -> {
                    Assertions.assertThat(wfRun.getThreadRuns(1).getNumber()).isEqualTo(2);
                    Assertions.assertThat(wfRun.getThreadRunsCount()).isEqualTo(2);
                })
                .waitForStatus(LHStatus.COMPLETED)
                .start();
    }
}
