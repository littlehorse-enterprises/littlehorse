package io.littlehorse.sdk.worker.internal;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.littlehorse.sdk.common.config.LHConfig;
import java.io.IOException;
import org.junit.jupiter.api.Test;

public class LHServerConnectionManagerTest {
    @Test
    public void shouldStopManagerWhenRetriesWhereExhausted() throws Exception {
        LHConfig mockConfig = mock();
        when(mockConfig.getWorkerThreads()).thenReturn(1);

        ConnectionManagerLivenessController mockedLivenessController = new ConnectionManagerLivenessController(100);
        LHServerConnectionManager connectionManager =
                new LHServerConnectionManager(mock(), mock(), mockConfig, mock(), mock(), mockedLivenessController);

        connectionManager.onError(new IOException());

        Thread.sleep(101);

        assertThat(mockedLivenessController.keepManagerRunning()).isEqualTo(false);
    }
}
