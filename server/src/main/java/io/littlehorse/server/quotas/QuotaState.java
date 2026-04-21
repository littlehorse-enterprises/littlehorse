package io.littlehorse.server.quotas;

import io.littlehorse.common.model.getable.global.acl.QuotaModel;

class QuotaState {

    private static final long WINDOW_MS = 500L;
    private static final long NANOS_PER_WINDOW = WINDOW_MS * 1_000_000;

    private int permitDebt; // Used to calculate retry delay
    private int permitsLeftInThisWindow; // Keeps track of whether we can accept requests in this window
    private int permitsPerWindow;
    private long lastRefillNanos;
    private boolean initialized;

    QuotaState() {
        this.permitDebt = 0;
        this.permitsPerWindow = 0;
        this.lastRefillNanos = 0L;
        this.permitsLeftInThisWindow = 0;
        this.initialized = false;
    }

    synchronized long recordRequestAndCalculateDelay(QuotaModel quota, int serverCount) {
        maybeRefreshWindow(quota, serverCount);
        permitsLeftInThisWindow--;
        return calculateDelayMs();
    }

    private long calculateDelayMs() {
        // If we still have permits, allow the request immediately.
        if (permitsLeftInThisWindow >= 0) {
            return 0L;
        }

        // Here's where we implement permit debt:
        // - Under high load, send increasingly high backoff to account for throttled requests
        //   which will come in again soon after when the retry delay expires
        // - the refresh() caps the debt to limit the recovery time
        // This is the way. Obi-Wan.
        permitDebt++;

        double numberOfWindowsToWait = 1.0 + ((double) permitDebt / permitsPerWindow);
        long delayMs = (long) (WINDOW_MS * numberOfWindowsToWait);
        return delayMs;
    }

    private void maybeRefreshWindow(QuotaModel quota, int serverCount) {
        long nowNanos = System.nanoTime();

        // The Quota or the number of servers may have changed!
        double requestsPerSec = Math.max(1.0, quota.getWriteRequestsPerSecond() / (double) serverCount);
        this.permitsPerWindow = (int) Math.ceil(Math.max(1.0, requestsPerSec * WINDOW_MS / 1000));

        if (!initialized) {
            this.permitDebt = 0;
            this.lastRefillNanos = nowNanos;
            this.permitsLeftInThisWindow = this.permitsPerWindow;
            this.initialized = true;
            return;
        }

        long elapsedWindows = (nowNanos - lastRefillNanos) / NANOS_PER_WINDOW;
        if (elapsedWindows > 0) {
            lastRefillNanos = nowNanos;
            this.permitsLeftInThisWindow = this.permitsPerWindow;

            // Re-calculate permit debt. This helps us give better throttling experience after
            // a large spikey batch dump.
            permitDebt -= permitsPerWindow * elapsedWindows;
            permitDebt = Math.max(0, permitDebt); // Permit debt can't be negative
        }
    }
}
