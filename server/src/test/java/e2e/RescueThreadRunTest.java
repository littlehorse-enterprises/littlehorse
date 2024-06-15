package e2e;

import static org.assertj.core.api.Assertions.assertThat;

import io.grpc.Status.Code;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.TaskStatus;
import io.littlehorse.sdk.common.proto.VariableMutationType;
import io.littlehorse.sdk.common.util.Arg;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.sdk.worker.WorkerContext;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.LHWorkflow;
import io.littlehorse.test.WorkflowVerifier;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.Test;

@LHTest
public class RescueThreadRunTest {

    private WorkflowVerifier verifier;

    private Map<String, Integer> wfRunToTimesFailed = new ConcurrentHashMap<>();

    @LHWorkflow("simple-rescue-threadrun")
    private Workflow simpleRescueThreadRun;

    @LHWorkflow("simple-rescue-threadrun")
    public Workflow getSimpleWorkflow() {
        return Workflow.newWorkflow("simple-rescue-threadrun", wf -> {
            WfRunVariable timesToFail = wf.addVariable("times-to-fail", 1);
            WfRunVariable didMutationHappen = wf.addVariable("did-mutation-happen", false);

            // Fail a configurable amount of times
            wf.execute("throw-error-x-times", timesToFail);
            wf.mutate(didMutationHappen, VariableMutationType.ASSIGN, true);

            // Should continue on for free
            wf.execute("no-rescue-needed");
        });
    }

    @Test
    void shouldExecuteFailedNodeAgainOnRescue() {
        // If we execute the failed Node again, we *SHOULD* observe the mutations.
        verifier.prepareRun(simpleRescueThreadRun, Arg.of("times-to-fail", 1))
                .waitForNodeRunStatus(0, 1, LHStatus.ERROR)
                .thenRescueThreadRun(0, false) // dont skip failed node: try again
                .waitForStatus(LHStatus.COMPLETED)
                .thenVerifyAllTaskRuns(0, taskRuns -> {
                    assertThat(taskRuns.size()).isEqualTo(3);
                    assertThat(taskRuns.get(0).getStatus()).isEqualTo(TaskStatus.TASK_FAILED);
                    assertThat(taskRuns.get(1).getTaskDefId().getName()).isEqualTo("throw-error-x-times");
                    assertThat(taskRuns.get(2).getTaskDefId().getName()).isEqualTo("no-rescue-needed");
                })
                .thenVerifyVariable(0, "did-mutation-happen", variable -> {
                    assertThat(variable.getBool()).isTrue();
                })
                .start();
    }

    @Test
    void shouldSkipFailedNodeRunOnRescue() {
        verifier.prepareRun(simpleRescueThreadRun, Arg.of("times-to-fail", 1))
                .waitForNodeRunStatus(0, 1, LHStatus.ERROR)
                .thenRescueThreadRun(0, true) // skip failed node
                .waitForStatus(LHStatus.COMPLETED)
                .thenVerifyAllTaskRuns(0, taskRuns -> {
                    assertThat(taskRuns.size()).isEqualTo(2);
                    assertThat(taskRuns.get(0).getStatus()).isEqualTo(TaskStatus.TASK_FAILED);
                    assertThat(taskRuns.get(1).getTaskDefId().getName()).isEqualTo("no-rescue-needed");
                })
                .thenVerifyVariable(0, "did-mutation-happen", variable -> {
                    assertThat(variable.getBool()).isFalse();
                })
                .start();
    }

    @Test
    void cantRescueIfThreadIsntError() {
        verifier.prepareRun(simpleRescueThreadRun, Arg.of("times-to-fail", 0)) // don't fail at all
                .waitForNodeRunStatus(0, 1, LHStatus.COMPLETED)
                .thenRescueThreadRun(0, true, exn -> {
                    assertThat(exn.getStatus().getCode()).isEqualTo(Code.FAILED_PRECONDITION);
                })
                .waitForStatus(LHStatus.COMPLETED)
                .start();
    }

    @LHTaskMethod("no-rescue-needed")
    public void noRescueNeeded() {
        // nothing to do
    }

    @LHTaskMethod("throw-error-x-times")
    public void throwErrorXTimes(int timesToFail, WorkerContext ctx) {
        String key = ctx.getWfRunId().getId();
        int timesFailed = wfRunToTimesFailed.computeIfAbsent(key, (k) -> {
            return 0;
        });
        if (timesFailed < timesToFail) {
            wfRunToTimesFailed.put(key, timesFailed + 1);
            throw new RuntimeException("hahaha");
        }
    }
}
