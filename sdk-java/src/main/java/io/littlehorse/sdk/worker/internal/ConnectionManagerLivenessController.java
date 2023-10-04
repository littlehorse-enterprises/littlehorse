package io.littlehorse.sdk.worker.internal;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class ConnectionManagerLivenessController {

    private final long timeoutInMilliseconds;
    private LocalDateTime failureOccurredAt;

    public ConnectionManagerLivenessController(long timeoutInMilliseconds) {
        this.timeoutInMilliseconds = timeoutInMilliseconds;
    }

    public void notifyFailure() {
        this.failureOccurredAt = LocalDateTime.now();
    }

    public boolean keepManagerRunning() {
        if (failureOccurredAt == null) {
            return true;
        }
        LocalDateTime upperLimit = this.failureOccurredAt.plus(timeoutInMilliseconds, ChronoUnit.MILLIS);
        return LocalDateTime.now().isBefore(upperLimit);
    }
}
