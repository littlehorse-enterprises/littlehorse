package io.littlehorse.server.quotas;

import io.littlehorse.common.model.getable.global.acl.QuotaModel;

class QuotaState {

    private static final long WINDOW_MS = 500L;
    private static final long NANOS_PER_WINDOW = WINDOW_MS * 1_000_000;
    private static final int MAX_WINDOWS_OF_DEBT = 5;

    private int availablePermits;
    private int capacityPerWindow;
    private long lastRefillNanos;
    private boolean initialized;

    QuotaState() {
        this.availablePermits = 0;
        this.capacityPerWindow = 0;
        this.lastRefillNanos = 0L;
        this.initialized = false;
    }

    long recordRequestAndCalculateDelay(QuotaModel quota, int serverCount) {
        maybeRefreshWindow(quota, serverCount);
        availablePermits -= 1.0;
        return calculateDelayMs();
    }

    private long calculateDelayMs() {
        // Here's where we implement permit debt:
        // - Under high load, send increasingly high backoff to account for throttled requests
        //   which will come in again soon after when the retry delay expires
        // - the refresh() caps the debt to limit the recovery time
        // This is the way. Obi-Wan.

        double permitDebt = -1.0 * availablePermits;
        double numberOfWindowsToWait = 1.0 + (permitDebt / capacityPerWindow);
        long delayMs = (long) (WINDOW_MS * numberOfWindowsToWait);
        return delayMs;
    }

    private void maybeRefreshWindow(QuotaModel quota, int serverCount) {
        long nowNanos = System.currentTimeMillis();

        double requestsPerSec = Math.max(1.0, quota.getWriteRequestsPerSecond() / (double) serverCount);
        this.capacityPerWindow = (int) Math.ceil(Math.max(1.0, requestsPerSec * WINDOW_MS / 1000));

        if (!initialized) {
            this.availablePermits = capacityPerWindow;
            this.lastRefillNanos = nowNanos;
            this.initialized = true;
            return;
        }

        long elapsedWindows = (nowNanos - lastRefillNanos) / NANOS_PER_WINDOW;
        if (elapsedWindows > 0) {
            lastRefillNanos = nowNanos;

            // In cases of super high load, we need to "cap the debt". This is the
            // "token bucket" pattern.
            if (availablePermits < MAX_WINDOWS_OF_DEBT * capacityPerWindow * -1) {
                availablePermits = MAX_WINDOWS_OF_DEBT * capacityPerWindow * -1;
            }

            // Refresh the available permits. Note that, with debt, availablePermits can be negative.
            availablePermits += capacityPerWindow * elapsedWindows;

            // Make sure permits didn't go above what it should be.
            availablePermits = Math.min(capacityPerWindow, availablePermits);
        }
    }
}
