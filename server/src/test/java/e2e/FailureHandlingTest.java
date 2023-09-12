package e2e;

import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.wfsdk.NodeOutput;
import io.littlehorse.sdk.wfsdk.SpawnedThread;
import io.littlehorse.sdk.wfsdk.WaitForThreadsNodeOutput;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.LHWorkflow;
import io.littlehorse.test.WorkflowVerifier;
import java.util.Map;
import org.junit.jupiter.api.Test;

@LHTest
public class FailureHandlingTest {

    private WorkflowVerifier workflowVerifier;

    @LHWorkflow("handle-error-wf")
    private Workflow handleErrorWf;

    @LHWorkflow("handle-exception-wf")
    private Workflow handleExceptionWf;

    @LHWorkflow("handle-any-exception-wf")
    private Workflow handleAnyExceptionWf;

    @Test
    public void shouldHandleAnyError() {
        workflowVerifier
                .prepareRun(handleErrorWf)
                .waitForStatus(LHStatus.COMPLETED)
                .waitForNodeRunStatus(0, 1, LHStatus.ERROR)
                .waitForNodeRunStatus(1, 1, LHStatus.COMPLETED)
                .waitForNodeRunStatus(0, 3, LHStatus.COMPLETED)
                .waitForStatus(LHStatus.COMPLETED)
                .start();
    }

    @Test
    public void shouldHandleSpecificException() {
        workflowVerifier
                .prepareRun(handleExceptionWf)
                .waitForStatus(LHStatus.COMPLETED)
                .start();
    }

    @Test
    public void shouldHandleAnyException() {
        workflowVerifier
                .prepareRun(handleAnyExceptionWf)
                .waitForStatus(LHStatus.ERROR)
                .start();
    }

    @LHWorkflow("handle-error-wf")
    public Workflow handleErrorWorkflow() {
        return new WorkflowImpl("example-exception-handler", thread -> {
            NodeOutput node = thread.execute("fail");
            thread.handleError(node, handler -> {
                handler.execute("my-handler");
            });
            thread.execute("my-task");
        });
    }

    @LHWorkflow("handle-exception-wf")
    public Workflow handleExceptionWorkflow() {
        return new WorkflowImpl("example-exception-handler", thread -> {
            SpawnedThread spawnThread = thread.spawnThread(
                    subThread -> {
                        subThread.fail("my-exception", "this is a exception");
                    },
                    "sub-thread",
                    Map.of());
            WaitForThreadsNodeOutput waitForThread = thread.waitForThreads(spawnThread);
            thread.handleException(waitForThread, "my-exception", handler -> {
                handler.execute("my-task");
            });
        });
    }

    @LHWorkflow("handle-any-exception-wf")
    public Workflow handleAnyExceptionWorkflow() {
        return new WorkflowImpl("example-exception-handler", thread -> {
            SpawnedThread spawnThread = thread.spawnThread(
                    subThread -> {
                        subThread.execute("fail");
                    },
                    "sub-thread",
                    Map.of());
            WaitForThreadsNodeOutput waitForThread = thread.waitForThreads(spawnThread);
            thread.handleException(waitForThread, handler -> {
                handler.execute("my-task");
            });
        });
    }

    @LHTaskMethod("fail")
    public String fail() {
        throw new RuntimeException("something went wrong!");
    }

    @LHTaskMethod("my-handler")
    public String handler() {
        return "Exception handled";
    }

    @LHTaskMethod("my-task")
    public String task() {
        return "Task executed";
    }
}
