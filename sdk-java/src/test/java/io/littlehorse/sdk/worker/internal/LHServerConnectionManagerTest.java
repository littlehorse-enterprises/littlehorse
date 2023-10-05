package io.littlehorse.sdk.worker.internal;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.LHPublicApiGrpc;
import io.littlehorse.sdk.common.proto.RegisterTaskWorkerResponse;
import io.littlehorse.sdk.common.proto.TaskDef;
import java.io.IOException;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LHServerConnectionManagerTest {

    static final long HEARTBEAT_INTERVAL_MS = 5000L;
    private final LHConfig mockConfig = mock();
    private final TaskDef mockTaskDef = mock();

    @BeforeEach
    public void globalSetUp() {
        mockConfig();
    }

    @Test
    public void connectionManagerKeepsRunningWhenAnErrorOccurredButTheTimeoutHasNotBeenExceeded() throws Exception {
        final long beforeTimeout = 101L;
        final long timeout = 100L;

        ConnectionManagerLivenessController livenessController = new ConnectionManagerLivenessController(timeout);
        LHServerConnectionManager connectionManager =
                new LHServerConnectionManager(mock(), mockTaskDef, mockConfig, mock(), mock(), livenessController);

        connectionManager.start();
        assertThat(connectionManager.isAlive()).isTrue();

        connectionManager.onError(new IOException());
        Thread.sleep(beforeTimeout);

        assertThat(connectionManager.isAlive()).isTrue();
    }

    @Test
    public void shouldStopManagerWhenRetriesWhereExhaustedAfterAnErrorHasOcurred() throws Exception {
        final TaskDef mockTaskDef = mock();
        final long afterTimeout = 101L;
        final long timeout = 100L;

        ConnectionManagerLivenessController livenessController = new ConnectionManagerLivenessController(timeout);
        LHServerConnectionManager connectionManager =
                new LHServerConnectionManager(mock(), mockTaskDef, mockConfig, mock(), mock(), livenessController);

        connectionManager.start();
        assertThat(connectionManager.isAlive()).isTrue();

        connectionManager.onError(new IOException());
        Thread.sleep(HEARTBEAT_INTERVAL_MS + afterTimeout);

        assertThat(connectionManager.isAlive()).isFalse();
    }

    @Test
    public void connectionManagerKeepsAlive() throws Exception {
        final long afterTimeout = 101L;
        final long timeout = 100L;

        ConnectionManagerLivenessController livenessController = new ConnectionManagerLivenessController(timeout);
        LHServerConnectionManager connectionManager =
                new LHServerConnectionManager(mock(), mockTaskDef, mockConfig, mock(), mock(), livenessController);

        connectionManager.start();
        assertThat(connectionManager.isAlive()).isTrue();

        Thread.sleep(HEARTBEAT_INTERVAL_MS + afterTimeout);

        assertThat(connectionManager.isAlive()).isTrue();
    }

    @Test
    public void connectionManagerIsNotHealthyIfAFailureHasBeenNotified() throws Exception{
        final long timeout = 100L;

        ConnectionManagerLivenessController livenessController = new ConnectionManagerLivenessController(timeout);
        LHServerConnectionManager connectionManager =
                new LHServerConnectionManager(mock(), mockTaskDef, mockConfig, mock(), mock(), livenessController);

        connectionManager.onError(new IOException());
        assertThat(connectionManager.isHealthy()).isFalse();
    }

    @Test
    public void connectionManagerRecoverHealthWhenASuccessHeartbeatIsNotified() throws Exception{
        final long timeout = 100L;

        ConnectionManagerLivenessController livenessController = new ConnectionManagerLivenessController(timeout);
        LHServerConnectionManager connectionManager =
                new LHServerConnectionManager(mock(), mockTaskDef, mockConfig, mock(), mock(), livenessController);

        connectionManager.onError(new IOException());
        assertThat(connectionManager.isHealthy()).isFalse();
        connectionManager.onNext(mock());
        assertThat(connectionManager.isHealthy()).isTrue();
    }

    @Test
    public void clusterIsHealthyWhenResponseIndicatesThat() throws Exception{
        final long timeout = 100L;

        ConnectionManagerLivenessController livenessController = new ConnectionManagerLivenessController(timeout);
        LHServerConnectionManager connectionManager =
                new LHServerConnectionManager(mock(), mockTaskDef, mockConfig, mock(), mock(), livenessController);

        connectionManager.onNext(
                RegisterTaskWorkerResponse.newBuilder()
                        .setIsClusterHealthy(true)
                        .build()
        );

        assertThat(connectionManager.isClusterHealthy()).isTrue();
    }

    @Test
    public void clusterIsUnhealthyWhenResponseIndicatesThat() throws Exception{
        final long timeout = 100L;

        ConnectionManagerLivenessController livenessController = new ConnectionManagerLivenessController(timeout);
        LHServerConnectionManager connectionManager =
                new LHServerConnectionManager(mock(), mockTaskDef, mockConfig, mock(), mock(), livenessController);

        connectionManager.onNext(
                RegisterTaskWorkerResponse.newBuilder()
                        .setIsClusterHealthy(false)
                        .build()
        );

        assertThat(connectionManager.isClusterHealthy()).isFalse();
    }

    @SneakyThrows
    private void mockConfig() {
        final LHPublicApiGrpc.LHPublicApiStub asyncStub = mock();

        when(mockConfig.getAsyncStub()).thenReturn(asyncStub);
        when(mockConfig.getWorkerThreads()).thenReturn(1);
        when(mockTaskDef.getName()).thenReturn("test");
        when(mockConfig.getClientId()).thenReturn("test-client-id");
        when(mockConfig.getConnectListener()).thenReturn("test-listener");
    }
}
