package e2e;

import io.littlehorse.sdk.common.proto.LHErrorType;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.util.Arg;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.LHWorkflow;
import io.littlehorse.test.WorkflowVerifier;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

@LHTest
public class DynamicTaskTest {

    @LHWorkflow("dynamic-task")
    private Workflow dynamicTask;

    private WorkflowVerifier verifier;

    @Test
    public void shouldExecuteDynamicTask() {
        verifier.prepareRun(dynamicTask, Arg.of("cluster-name", "summerlin"))
                .waitForStatus(LHStatus.COMPLETED)
                .thenVerifyTaskRun(0, 1, taskRun -> {
                    Assertions.assertThat(taskRun.getTaskDefId().getName()).isEqualTo("summerlin");
                })
                .thenVerifyTaskRun(0, 2, taskRun -> {
                    Assertions.assertThat(taskRun.getTaskDefId().getName())
                            .isEqualTo("dynamic-create-cluster-summerlin");
                })
                .start();
    }

    @Test
    public void shouldFailOnMissingTask() {
        verifier.prepareRun(dynamicTask, Arg.of("cluster-name", "not-a-real-cluster"))
                .waitForStatus(LHStatus.ERROR)
                .thenVerifyNodeRun(0, 1, nodeRun -> {
                    Assertions.assertThat(nodeRun.getStatus()).isEqualTo(LHStatus.ERROR);
                    Assertions.assertThat(nodeRun.getFailures(0).getFailureName())
                            .isEqualTo(LHErrorType.VAR_SUB_ERROR.toString());
                    Assertions.assertThat(nodeRun.getFailures(0).getMessage()).contains("not-a-real-cluster");
                })
                .start();
    }

    @LHWorkflow("dynamic-task")
    public Workflow getBasic() {
        return new WorkflowImpl("dynamic-task", wf -> {
            WfRunVariable clusterName =
                    wf.addVariable("cluster-name", VariableType.STR).required();
            wf.execute(clusterName);
            wf.execute(wf.format("dynamic-create-cluster-{0}", clusterName));
        });
    }

    @LHTaskMethod("dynamic-create-cluster-summerlin")
    public void createSummerlinCluster() {}

    @LHTaskMethod("summerlin")
    public void summerlin() {}
}
