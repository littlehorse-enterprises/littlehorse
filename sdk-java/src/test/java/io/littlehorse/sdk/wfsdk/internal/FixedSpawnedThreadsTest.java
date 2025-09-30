package io.littlehorse.sdk.wfsdk.internal;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.littlehorse.sdk.common.proto.WaitForThreadsNode;
import io.littlehorse.sdk.wfsdk.SpawnedThread;
import io.littlehorse.sdk.wfsdk.ThreadFunc;
import io.littlehorse.sdk.wfsdk.WorkflowThread;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FixedSpawnedThreadsTest {
    ThreadFunc threadFunction = new ThreadFunc() {
        @Override
        public void threadFunction(WorkflowThread thread) {}
    };

    private WorkflowThreadImpl workflowThread;
    private final SpawnedThread spawnedThread1 = mock();
    private final SpawnedThread spawnedThread2 = mock();

    private final FixedSpawnedThreads fixedSpawnedThreads = new FixedSpawnedThreads(spawnedThread1, spawnedThread2);

    @BeforeEach
    void setup() {
        WorkflowImpl workflow = new WorkflowImpl("my-workflow", threadFunction);
        workflowThread = new WorkflowThreadImpl("my-thread", workflow, threadFunction);

        when(spawnedThread1.getThreadNumberVariable())
                .thenReturn(WfRunVariableImpl.createPrimitiveVar("thread-1", 1, workflowThread));
        when(spawnedThread2.getThreadNumberVariable())
                .thenReturn(WfRunVariableImpl.createPrimitiveVar("thread-2", 2, workflowThread));
    }

    @Test
    void shouldBuildNodeForTwoThreadsToWaitFor() {
        WaitForThreadsNode waitForThreadsNode = fixedSpawnedThreads.buildNode();
        assertThat(waitForThreadsNode.getThreads().getThreadsList()).hasSize(2);
    }

    @Test
    void shouldThrowIllegalArgumentExceptionIfThereIsANonIntegerVar() {
        doReturn(WfRunVariableImpl.createPrimitiveVar("thread-2", "2", workflowThread))
                .when(spawnedThread2)
                .getThreadNumberVariable();
        Throwable exception = catchThrowable(fixedSpawnedThreads::buildNode);
        assertThat(exception).isInstanceOfAny(IllegalArgumentException.class);
    }
}
