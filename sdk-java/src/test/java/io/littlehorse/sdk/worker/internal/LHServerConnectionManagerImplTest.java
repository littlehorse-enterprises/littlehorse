package io.littlehorse.sdk.worker.internal;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc;
import io.littlehorse.sdk.common.proto.TaskDef;
import io.littlehorse.sdk.common.proto.TaskDefId;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;

public class LHServerConnectionManagerImplTest {

    static final long HEARTBEAT_INTERVAL_MS = 5000L;
    private final LHConfig mockConfig = mock();
    private final TaskDef mockTaskDef = mock();

    @BeforeEach
    public void globalSetUp() {
        mockConfig();
    }

    @SneakyThrows
    private void mockConfig() {
        final LittleHorseGrpc.LittleHorseStub asyncStub = mock();

        when(mockConfig.getAsyncStub()).thenReturn(asyncStub);
        when(mockConfig.getWorkerThreads()).thenReturn(1);
        when(mockTaskDef.getId())
                .thenReturn(TaskDefId.newBuilder().setName("test").build());
        when(mockConfig.getTaskWorkerId()).thenReturn("test-client-id");
        when(mockConfig.getConnectListener()).thenReturn("test-listener");
    }
}
