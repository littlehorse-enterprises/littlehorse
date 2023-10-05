package io.littlehorse.sdk.worker;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.littlehorse.sdk.worker.internal.LHServerConnectionManager;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled("WIP")
public class LHTaskWorkerTest {

    private final LHServerConnectionManager manager = mock();

    @Test
    public void theWorkerIsHealthyIfLHServerIsReachable() throws Exception {
        final LHTaskWorker worker = new LHTaskWorker(new GreetWorker(), "test_task", mock(), manager);
        // when(manager.isRunning()).thenReturn(true);
        assertThat(worker.isHealthy()).isEqualTo(true);
    }

    @Test
    public void theWorkerIsNotHealthyIfLHServerIsNotReachable() throws Exception {
        final LHTaskWorker worker = new LHTaskWorker(new GreetWorker(), "test_task", mock(), manager);
        // when(manager.isRunning()).thenReturn(false);
        assertThat(worker.isHealthy()).isEqualTo(false);
    }
}

class GreetWorker {
    @LHTaskMethod("greet-task")
    public void greet() {
        System.out.println("Greeting");
    }
}
