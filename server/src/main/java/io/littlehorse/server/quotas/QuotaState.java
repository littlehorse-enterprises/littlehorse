package io.littlehorse.server.quotas;

class QuotaState {

    private static final long WINDOW_MS = 500L;
    private static final long NANOS_PER_SECOND = 1_000_000_000L;
    private static final int MAX_WINDOWS_OF_DEBT = 5;

    private double availablePermits;
    private long lastRefillNanos;
    private boolean initialized;

    QuotaState() {
        this.availablePermits = 0.0;
        this.lastRefillNanos = 0L;
        this.initialized = false;
    }

    long recordRequestAndCalculateDelay(int writeRequestsPerSecond, int serverCount, long nowNanos) {
        double ratePerSecond = perServerRate(writeRequestsPerSecond, serverCount);
        double permitsPerWindow = ratePerSecond * (WINDOW_MS / 1000.0);
        refresh(ratePerSecond, nowNanos);
        availablePermits -= 1.0;

        if (availablePermits >= 0) {
            return 0L;
        }

        // Here's where we implement permit debt:
        // - Under high load, send increasingly high backoff to account for throttled requests
        //   which will come in again soon after when the retry delay expires
        // - the refresh() caps the debt to limit the recovery time
        // This is the way. Obi-Wan.

        double permitDebt = -1.0 * availablePermits;
        double numberOfWindowsToWait = 1.0 + (permitDebt / permitsPerWindow);
        long delayMs = (long) (WINDOW_MS * numberOfWindowsToWait);

        return delayMs;
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

            // Cap the debt at MAX_WINDOWS_OF_DEBT upon each refresh.
            if (availablePermits < -1 * MAX_WINDOWS_OF_DEBT * capacity) {
                availablePermits = -1 * MAX_WINDOWS_OF_DEBT * capacity;
            }
            lastRefillNanos = nowNanos;
        }
    }
}
