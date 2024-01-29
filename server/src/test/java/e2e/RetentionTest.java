package e2e;

import io.littlehorse.sdk.common.proto.ThreadRetentionPolicy;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.LHWorkflow;

@LHTest(externalEventNames = {RetentionTest.RETENTION_EVENT_1})
public class RetentionTest {

    public static final String RETENTION_EVENT_1 = "retention-event-1";

    @LHWorkflow("child-thread-retention-test")
    public Workflow buildRetentionWorkflow() {
        return Workflow.newWorkflow("retention-test", wf -> {
            wf.spawnThread(
                    child -> {
                        child.withRetentionPolicy(ThreadRetentionPolicy.newBuilder()
                                .setSecondsAfterThreadTermination(1)
                                .build());
                        child.execute("my-task");
                    },
                    "child",
                    null);

            wf.waitForEvent(RETENTION_EVENT_1);
        });
    }
}
