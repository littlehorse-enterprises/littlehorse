package io.littlehorse.sdk.worker.internal;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class ConnectionManagerLivenessController {

    private final long timeoutInMilliseconds;
    private LocalDateTime failureOccurredAt;
    private boolean isClusterHealthy;

    public ConnectionManagerLivenessController(long timeoutInMilliseconds) {
        this.timeoutInMilliseconds = timeoutInMilliseconds;
    }

    public void notifyFailure() {
        this.failureOccurredAt = LocalDateTime.now();
    }

    public void notifySuccessfulConnection() {
        this.failureOccurredAt = null;
    }

    public boolean isFailureDetected(){
        return this.failureOccurredAt != null;
    }

    public boolean keepManagerRunning() {
        if (failureOccurredAt == null) {
            return true;
        }
        LocalDateTime upperLimit = this.failureOccurredAt.plus(timeoutInMilliseconds, ChronoUnit.MILLIS);
        return LocalDateTime.now().isBefore(upperLimit);
    }

    public void notifyClusterHealthy(boolean isClusterHealthy) {
        this.isClusterHealthy = isClusterHealthy;
    }

    public boolean isClusterHealthy() {
        return this.isClusterHealthy;
    }
}
