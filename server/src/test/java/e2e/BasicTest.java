package e2e;

import static org.junit.jupiter.api.Assertions.assertThrows;

import io.grpc.Status.Code;
import io.grpc.StatusRuntimeException;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.WfRunId;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.LHWorkflow;
import io.littlehorse.test.WithWorkers;
import io.littlehorse.test.WorkflowVerifier;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

@LHTest
@WithWorkers(value = "basicWorker")
@WithWorkers(value = "basicWorker2", lhMethods = "five")
public class BasicTest {

    @LHWorkflow("test-basic")
    private Workflow basicWf;

    private WorkflowVerifier verifier;

    @Test
    @WithWorkers(value = "basicWorker3")
    public void shouldDoBasic() {
        verifier.prepareRun(basicWf).waitForStatus(LHStatus.COMPLETED).start();
    }

    @LHWorkflow("test-basic")
    public Workflow getBasic() {
        return new WorkflowImpl("test-basic", thread -> {
            thread.execute("one");
            thread.execute("two");
            thread.execute("three");
            thread.execute("five");
            thread.execute("seven");
        });
    }

    @Test
    public void runWfShouldFailWithInvalidId() {
        StatusRuntimeException caught = assertThrows(StatusRuntimeException.class, () -> {
            verifier.prepareRun(basicWf)
                    .waitForStatus(LHStatus.COMPLETED)
                    .start(WfRunId.newBuilder().setId("my_workflow").build());
        });

        Assertions.assertThat(caught.getStatus().getCode()).isEqualTo(Code.INVALID_ARGUMENT);
    }

    public Object basicWorker() {
        return new BasicWorker();
    }

    public Object basicWorker2() {
        return new BasicWorker2();
    }

    public Object basicWorker3() {
        return new BasicWorker3();
    }

    public class BasicWorker {

        @LHTaskMethod("one")
        public boolean one() {
            return true;
        }

        @LHTaskMethod("two")
        public boolean two() {
            return true;
        }

        @LHTaskMethod("three")
        public boolean three() {
            return true;
        }
    }

    public class BasicWorker2 {

        @LHTaskMethod("four")
        public boolean one() {
            return true;
        }

        @LHTaskMethod("five")
        public boolean two() {
            return true;
        }

        @LHTaskMethod("six")
        public boolean three() {
            return true;
        }
    }

    public class BasicWorker3 {

        @LHTaskMethod("seven")
        public boolean one() {
            return true;
        }
    }
}
