package io.littlehorse.sdk.worker.internal;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class ConnectionManagerLivenessControllerTest {

    @Test
    public void indicatesThatManagerShouldStopRunningWhenTimeoutIsOverpassed() throws Exception {
        long timeoutInMilliseconds = 100;
        ConnectionManagerLivenessController livenessController =
                new ConnectionManagerLivenessController(timeoutInMilliseconds);
        livenessController.notifyCallFailure();
        Thread.sleep(timeoutInMilliseconds);

        assertThat(livenessController.keepManagerRunning()).isEqualTo(false);
    }

    @Test
    public void indicatesThatManagerShouldKeepRunningWhenTimeoutIsNotReached() {
        long timeoutInMilliseconds = 100;
        ConnectionManagerLivenessController livenessController =
                new ConnectionManagerLivenessController(timeoutInMilliseconds);
        livenessController.notifyCallFailure();

        assertThat(livenessController.keepManagerRunning()).isEqualTo(true);
    }

    @Test
    public void indicatesThatManagerShouldKeepRunningWhenTimeoutIsZero() {
        long timeoutInMilliseconds = 0;
        ConnectionManagerLivenessController livenessController =
                new ConnectionManagerLivenessController(timeoutInMilliseconds);
        livenessController.notifyCallFailure();

        assertThat(livenessController.keepManagerRunning()).isEqualTo(false);
    }

    @Test
    public void indicatesThatManagerShouldKeepRunningWhenThereAreNoReportedFailures() {
        ConnectionManagerLivenessController livenessController = new ConnectionManagerLivenessController(100);
        assertThat(livenessController.keepManagerRunning()).isEqualTo(true);
    }
}
