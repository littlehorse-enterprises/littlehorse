package io.littlehorse.sdk.wfsdk.internal;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.littlehorse.sdk.common.proto.WaitForThreadsNode;
import io.littlehorse.sdk.wfsdk.SpawnedThread;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FixedSpawnThreadsTest {

    private final SpawnedThread spawnedThread1 = mock();
    private final SpawnedThread spawnedThread2 = mock();

    private final FixedSpawnThreads fixedSpawnThreads = new FixedSpawnThreads(spawnedThread1, spawnedThread2);

    @BeforeEach
    void setup() {
        doReturn(new WfRunVariableImpl("thread-1", 1)).when(spawnedThread1).getThreadNumberVariable();
        doReturn(new WfRunVariableImpl("thread-2", 2)).when(spawnedThread2).getThreadNumberVariable();
    }

    @Test
    void shouldBuildNodeForTwoThreadsToWaitFor() {
        WaitForThreadsNode waitForThreadsNode = fixedSpawnThreads.buildNode();
        assertThat(waitForThreadsNode.getThreadsList()).hasSize(2);
    }

    @Test
    void shouldThrowIllegalArgumentExceptionIfThereIsANonIntegerVar() {
        doReturn(new WfRunVariableImpl("thread-2", "2")).when(spawnedThread2).getThreadNumberVariable();
        Throwable exception = catchThrowable(fixedSpawnThreads::buildNode);
        assertThat(exception).isInstanceOfAny(IllegalArgumentException.class);
    }
}
