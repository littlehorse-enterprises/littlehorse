package io.littlehorse.sdk.worker.internal;

import io.littlehorse.sdk.common.proto.RegisterTaskWorkerResponse;
import java.time.LocalDateTime;

public class LHLivenessController {

    private LocalDateTime failureOccurredAt;
    private boolean isClusterHealthy = true;
    private boolean isRunning = true;

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
        return isRunning;
    }

    public void stop() {
        isRunning = false;
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
