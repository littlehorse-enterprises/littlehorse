package io.littlehorse.sdk.wfsdk.internal;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.littlehorse.sdk.common.proto.WaitForThreadsNode;
import io.littlehorse.sdk.wfsdk.SpawnedThread;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FixedSpawnedThreadsTest {

    private final SpawnedThread spawnedThread1 = mock();
    private final SpawnedThread spawnedThread2 = mock();

    private final FixedSpawnedThreads fixedSpawnedThreads = new FixedSpawnedThreads(spawnedThread1, spawnedThread2);

    @BeforeEach
    void setup() {
        when(spawnedThread1.getThreadNumberVariable()).thenReturn(new WfRunVariableImpl("thread-1", 1, null));
        when(spawnedThread2.getThreadNumberVariable()).thenReturn(new WfRunVariableImpl("thread-2", 2, null));
    }

    @Test
    void shouldBuildNodeForTwoThreadsToWaitFor() {
        WaitForThreadsNode waitForThreadsNode = fixedSpawnedThreads.buildNode();
        assertThat(waitForThreadsNode.getThreads().getThreadsList()).hasSize(2);
    }

    @Test
    void shouldThrowIllegalArgumentExceptionIfThereIsANonIntegerVar() {
        doReturn(new WfRunVariableImpl("thread-2", "2", null))
                .when(spawnedThread2)
                .getThreadNumberVariable();
        Throwable exception = catchThrowable(fixedSpawnedThreads::buildNode);
        assertThat(exception).isInstanceOfAny(IllegalArgumentException.class);
    }
}
