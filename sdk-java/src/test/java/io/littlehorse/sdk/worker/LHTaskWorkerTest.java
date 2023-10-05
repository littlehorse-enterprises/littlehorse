package io.littlehorse.sdk.worker;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.littlehorse.sdk.worker.internal.LHServerConnectionManager;
import org.junit.jupiter.api.Test;

public class LHTaskWorkerTest {
    private final LHServerConnectionManager manager = mock();

    @Test
    public void theWorkerIsHealthyIfNoCallFailureHasBeenNotifiedAndClusterIsHealthy() throws Exception {
        final LHTaskWorker worker = new LHTaskWorker(new GreetWorker(), "test_task", mock(), manager);
        when(manager.wasThereAnyCallFailure()).thenReturn(true);

        assertThat(worker.isHealthy()).isEqualTo(true);
    }

    @Test
    public void theWorkerIsUnhealthyIfConnectionIsUnhealthy() throws Exception {
        final LHTaskWorker worker = new LHTaskWorker(new GreetWorker(), "test_task", mock(), manager);
        when(manager.wasThereAnyCallFailure()).thenReturn(false);
        assertThat(worker.isHealthy()).isEqualTo(false);
    }

    @Test
    public void theWorkerIsUnhealthyWhenTheLHServerIsUnhealthy() throws Exception {
        final LHTaskWorker worker = new LHTaskWorker(new GreetWorker(), "test_task", mock(), manager);
    }
}

class GreetWorker {
    @LHTaskMethod("greet-task")
    public void greet() {
        System.out.println("Greeting");
    }
}
