package io.littlehorse.sdk.worker.internal;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.LHPublicApiGrpc;
import io.littlehorse.sdk.common.proto.TaskDef;
import java.io.IOException;
import org.junit.jupiter.api.Test;

public class LHServerConnectionManagerTest {

    static final long HEARTBEAT_INTERVAL_MS = 5000L;

    @Test
    public void shouldStopManagerWhenRetriesWhereExhaustedAfterAnErrorHasOcurred() throws Exception {
        final LHConfig mockConfig = mock();
        final TaskDef mockTaskDef = mock();
        final long afterTimeout = 101L;
        final long timeout = 100L;
        final LHPublicApiGrpc.LHPublicApiStub asyncStub = mock();

        when(mockConfig.getAsyncStub()).thenReturn(asyncStub);
        when(mockConfig.getWorkerThreads()).thenReturn(1);
        when(mockTaskDef.getName()).thenReturn("test");
        when(mockConfig.getClientId()).thenReturn("test-client-id");
        when(mockConfig.getConnectListener()).thenReturn("test-listener");

        ConnectionManagerLivenessController mockedLivenessController = new ConnectionManagerLivenessController(timeout);
        LHServerConnectionManager connectionManager = new LHServerConnectionManager(
                mock(), mockTaskDef, mockConfig, mock(), mock(), mockedLivenessController);

        connectionManager.start();
        assertThat(connectionManager.isAlive()).isTrue();

        connectionManager.onError(new IOException());
        Thread.sleep(HEARTBEAT_INTERVAL_MS + afterTimeout);

        assertThat(connectionManager.isAlive()).isFalse();
    }
}
