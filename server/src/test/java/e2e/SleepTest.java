package e2e;

import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.SleepNodeRun;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.util.Arg;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.LHWorkflow;
import io.littlehorse.test.WorkflowVerifier;
import java.time.Duration;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

@LHTest
public class SleepTest {

    private WorkflowVerifier verifier;

    @LHWorkflow("sleep-test")
    private Workflow sleepTest;

    @Test
    void shouldSleepUntilTimestampInFuture() {
        long twoSecondsFromNow = System.currentTimeMillis() + 2000;
        verifier.prepareRun(sleepTest, Arg.of("timestamp-to-wait-for", twoSecondsFromNow))
                .waitForNodeRunStatus(0, 1, LHStatus.COMPLETED, Duration.ofSeconds(3))
                .thenVerifyNodeRun(0, 1, nodeRun -> {
                    System.out.println(nodeRun);
                    // Should have finished roughly at the scheduled time
                    long actualFinishTime =
                            LHLibUtil.fromProtoTs(nodeRun.getEndTime()).getTime();
                    long difference = actualFinishTime - twoSecondsFromNow;
                    // Must be AFTER the scheduled time but within 2 seconds.
                    Assertions.assertThat(difference).isNotNegative();
                    Assertions.assertThat(difference).isLessThan(2000);

                    SleepNodeRun snr = nodeRun.getSleep();
                    Assertions.assertThat(LHLibUtil.fromProtoTs(snr.getMaturationTime())
                                    .getTime())
                            .isEqualTo(twoSecondsFromNow);
                })
                .start();
    }

    @Test
    void shouldCompleteWhenTimestampIsInThePast() {
        long twoSecondsAgo = System.currentTimeMillis() - 2000;
        verifier.prepareRun(sleepTest, Arg.of("timestamp-to-wait-for", twoSecondsAgo))
                .waitForNodeRunStatus(0, 1, LHStatus.COMPLETED, Duration.ofSeconds(2))
                .waitForStatus(LHStatus.COMPLETED)
                .start();
    }

    @LHWorkflow("sleep-test")
    public Workflow getWorkflow() {
        return Workflow.newWorkflow("sleep-test", wf -> {
            WfRunVariable myVar = wf.addVariable("timestamp-to-wait-for", VariableType.INT);
            wf.sleepUntil(myVar);
        });
    }
}
