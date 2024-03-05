package e2e;

import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.LHWorkflow;
import io.littlehorse.test.WorkflowVerifier;
import org.junit.jupiter.api.Test;

@LHTest
public class WorkflowEventsTest {

    @LHWorkflow("events")
    private Workflow eventsWf;

    private WorkflowVerifier verifier;

    @Test
    public void shouldDoBasic() {
        // Commented out until we add the ability to create a WorkflowEventDef to the test
        // framework.
        // verifier.prepareRun(eventsWf).waitForStatus(LHStatus.COMPLETED).start();
    }

    @LHWorkflow("events")
    public Workflow eventsWf() {
        return new WorkflowImpl("events", entrypoint -> {
            entrypoint.throwEvent("user-created", 20);
        });
    }
}
