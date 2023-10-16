package io.littlehorse.sdk.worker.internal;

import io.littlehorse.sdk.common.proto.RegisterTaskWorkerResponse;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class ConnectionManagerLivenessController {

    private final long timeoutInMilliseconds;
    private LocalDateTime failureOccurredAt;
    private boolean isClusterHealthy;

    public ConnectionManagerLivenessController(long timeoutInMilliseconds) {
        this.timeoutInMilliseconds = timeoutInMilliseconds;
    }

    public void notifyCallFailure() {
        if (failureOccurredAt == null) {
            this.failureOccurredAt = LocalDateTime.now();
        }
    }

    public void notifySuccessfulCall() {
        this.failureOccurredAt = null;
    }

    public boolean wasFailureNotified() {
        return this.failureOccurredAt != null;
    }

    public boolean keepManagerRunning() {
        if (failureOccurredAt == null) {
            return true;
        }
        LocalDateTime upperLimit = this.failureOccurredAt.plus(timeoutInMilliseconds, ChronoUnit.MILLIS);
        return LocalDateTime.now().isBefore(upperLimit);
    }

    public boolean isClusterHealthy() {
        return this.isClusterHealthy;
    }

    public void establishClusterHealth(RegisterTaskWorkerResponse response) {
        if (response.hasIsClusterHealthy()) {
            this.isClusterHealthy = response.getIsClusterHealthy();
        } else {
            this.isClusterHealthy = true;
        }
    }
}
