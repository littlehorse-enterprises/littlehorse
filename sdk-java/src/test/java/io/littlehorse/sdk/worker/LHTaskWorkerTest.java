package io.littlehorse.sdk.worker;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.littlehorse.sdk.worker.internal.LHServerConnectionManager;
import org.junit.jupiter.api.Test;

public class LHTaskWorkerTest {
    private final LHServerConnectionManager manager = mock();

    @Test
    public void theWorkerIsHealthyIfNoCallFailureHasBeenNotifiedAndClusterIsHealthy() {
        final LHTaskWorker worker = new LHTaskWorker(new GreetWorker(), "test_task", mock(), manager);

        when(manager.wasThereAnyFailure()).thenReturn(false);
        when(manager.isClusterHealthy()).thenReturn(true);

        LHTaskWorkerHealth workerHealth = worker.healthStatus();

        assertThat(workerHealth.isHealthy()).isTrue();
        assertThat(workerHealth.getReason()).isEqualTo(LHTaskWorkerHealthReason.HEALTHY);
    }

    @Test
    public void theWorkerIsUnhealthyIfAFailureHasBeenNotifiedEvenIfClusterIsHealthy() {
        final LHTaskWorker worker = new LHTaskWorker(new GreetWorker(), "test_task", mock(), manager);

        when(manager.wasThereAnyFailure()).thenReturn(true);
        when(manager.isClusterHealthy()).thenReturn(true);

        assertThat(worker.healthStatus().isHealthy()).isFalse();
        assertThat(worker.healthStatus().getReason()).isEqualTo(LHTaskWorkerHealthReason.UNHEALTHY);
    }

    @Test
    public void theWorkerIsUnhealthyIfNoFailureOnCallsButClusterIsUnhealthy() {
        final LHTaskWorker worker = new LHTaskWorker(new GreetWorker(), "test_task", mock(), manager);

        when(manager.wasThereAnyFailure()).thenReturn(false);
        when(manager.isClusterHealthy()).thenReturn(false);

        assertThat(worker.healthStatus().isHealthy()).isFalse();
        assertThat(worker.healthStatus().getReason()).isEqualTo(LHTaskWorkerHealthReason.SERVER_REBALANCING);
    }

    @Test
    public void theWorkerIsUnhealthyIfFailureOnCallsAndClusterIsUnhealthy() {
        final LHTaskWorker worker = new LHTaskWorker(new GreetWorker(), "test_task", mock(), manager);

        when(manager.wasThereAnyFailure()).thenReturn(true);
        when(manager.isClusterHealthy()).thenReturn(false);

        assertThat(worker.healthStatus().isHealthy()).isFalse();
        assertThat(worker.healthStatus().getReason()).isEqualTo(LHTaskWorkerHealthReason.SERVER_REBALANCING);
    }
}

class GreetWorker {
    @LHTaskMethod("greet-task")
    public void greet() {
        System.out.println("Greeting");
    }
}
