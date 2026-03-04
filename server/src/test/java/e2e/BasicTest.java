package e2e;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.grpc.Status.Code;
import io.grpc.StatusRuntimeException;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.WfRunId;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
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
@WithWorkers(value = "basicWorker2", lhMethods = "five")
public class BasicTest {

    @LHWorkflow("test-basic")
    private Workflow basicWf;

    @LHWorkflow("test-reused-tasks")
    private Workflow reusedWorkerWf;

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

    @LHWorkflow("test-reused-tasks")
    public Workflow getPepe() {
        return new WorkflowImpl("test-reused-tasks", thread -> {
            WfRunVariable output = thread.declareStr("output");
            output.assign(thread.execute("reused-task"));
        });
    }

    @Test
    public void runWfShouldFailWithInvalidId() {
        StatusRuntimeException caught = assertThrows(StatusRuntimeException.class, () -> {
            verifier.prepareRun(basicWf)
                    .waitForStatus(LHStatus.COMPLETED)
                    .start(WfRunId.newBuilder().setId("my_workflow").build());
        });

        assertThat(caught.getStatus().getCode()).isEqualTo(Code.INVALID_ARGUMENT);
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

    public Object reusedWorker() {
        return new ReusedWorker();
    }

    /**
     * The test-utils library was not cleaning the context when @WithWorkers annotation was placed
     * at the method level. As a result, if the same task was used in two different tests, then the
     * second test would fail. Do not remove this test, it's testing that the bug got fixed and is not
     * introduced again
     */
    @Test
    @WithWorkers("reusedWorker")
    public void shouldCompleteWithReusedTask() {
        verifier.prepareRun(reusedWorkerWf)
                .waitForStatus(LHStatus.COMPLETED)
                .thenVerifyVariable(0, "output", variableValue -> {
                    assertThat(variableValue.getStr()).isEqualTo("something");
                })
                .start();
    }

    /**
     * The test-utils library was not cleaning the context when @WithWorkers annotation was placed
     * at the method level. As a result, if the same task was used in two different tests, then the
     * second test would fail. Do not remove this test, it's testing that the bug got fixed and is not
     * introduced again
     */
    @Test
    @WithWorkers("reusedWorker")
    public void shouldCompleteWithReusedTask2() {
        verifier.prepareRun(reusedWorkerWf)
                .waitForStatus(LHStatus.COMPLETED)
                .thenVerifyVariable(0, "output", variableValue -> {
                    assertThat(variableValue.getStr()).isEqualTo("something");
                })
                .start();
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

    public class ReusedWorker {

        @LHTaskMethod("reused-task")
        public String reusedTask() {
            return "something";
        }
    }
}
