package e2e;

import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.LHWorkflow;
import io.littlehorse.test.WithWorkers;
import io.littlehorse.test.WorkflowVerifier;
import org.junit.jupiter.api.Test;

@LHTest
@WithWorkers(value = "basicWorker")
@WithWorkers(value = "basicWorker2")
public class BasicTest {

    @LHWorkflow("test-basic")
    private Workflow basicWf;

    private LHConfig config;

    private WorkflowVerifier verifier;

    @Test
    public void shouldDoBasic() {
        verifier.prepareRun(basicWf).waitForStatus(LHStatus.COMPLETED).start();
    }

    @LHWorkflow("test-basic")
    public Workflow getBasic() {
        return new WorkflowImpl("test-basic", thread -> {
            thread.execute("ag-one");
            thread.execute("ag-two");
            thread.execute("ag-three");
            thread.execute("ag-five");
        });
    }

    public Object basicWorker() {
        return new BasicWorker();
    }

    public Object basicWorker2() {
        return new BasicWorker2();
    }

    public class BasicWorker {

        @LHTaskMethod("ag-one")
        public boolean one() {
            return true;
        }

        @LHTaskMethod("ag-two")
        public boolean two() {
            return true;
        }

        @LHTaskMethod("ag-three")
        public boolean three() {
            return true;
        }
    }

    public class BasicWorker2 {

        @LHTaskMethod("ag-four")
        public boolean one() {
            return true;
        }

        @LHTaskMethod("ag-five")
        public boolean two() {
            return true;
        }

        @LHTaskMethod("ag-six")
        public boolean three() {
            return true;
        }
    }
}
