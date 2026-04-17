package io.littlehorse.server.quotas;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
class QuotaState {

    private static final long WINDOW_MS = 500L;
    private static final long NANOS_PER_SECOND = 1_000_000_000L;

    private double availablePermits;
    private long lastRefillNanos;
    private boolean initialized;

    QuotaState() {
        this.availablePermits = 0.0;
        this.lastRefillNanos = 0L;
        this.initialized = false;
    }

    QuotaState(double availablePermits, long lastRefillNanos) {
        this.availablePermits = availablePermits;
        this.lastRefillNanos = lastRefillNanos;
        this.initialized = true;
    }

    long previewRetryDelayMillis(int writeRequestsPerSecond, int serverCount, long nowNanos) {
        double ratePerSecond = perServerRate(writeRequestsPerSecond, serverCount);
        refresh(ratePerSecond, nowNanos);
        if (availablePermits >= 1.0) {
            return 0L;
        }

        double missingPermits = 1.0 - availablePermits;
        long delayMs = (long) Math.ceil((missingPermits / ratePerSecond) * 1_000.0);
        long roundedDelayMs = ((Math.max(delayMs, 1L) + WINDOW_MS - 1) / WINDOW_MS) * WINDOW_MS;
        return Math.max(WINDOW_MS, roundedDelayMs);
    }

    void recordAccepted(int writeRequestsPerSecond, int serverCount, long nowNanos) {
        double ratePerSecond = perServerRate(writeRequestsPerSecond, serverCount);
        refresh(ratePerSecond, nowNanos);
        availablePermits -= 1.0;
    }

    private static double perServerRate(int writeRequestsPerSecond, int serverCount) {
        return Math.max(1.0, writeRequestsPerSecond / (double) serverCount);
    }

    private void refresh(double ratePerSecond, long nowNanos) {
        double capacity = Math.max(1.0, ratePerSecond * WINDOW_MS / 1_000.0);
        if (!initialized) {
            availablePermits = capacity;
            lastRefillNanos = nowNanos;
            initialized = true;
            return;
        }

        double elapsedSeconds = (nowNanos - lastRefillNanos) / (double) NANOS_PER_SECOND;
        if (elapsedSeconds > 0) {
            availablePermits = Math.min(capacity, availablePermits + (elapsedSeconds * ratePerSecond));
            lastRefillNanos = nowNanos;
        }
    }
}
