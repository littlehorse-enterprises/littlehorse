package io.littlehorse.sdk.worker;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Snapshot of task worker health state.
 */
@Builder
@Getter
@EqualsAndHashCode
public class LHTaskWorkerHealth {
    private boolean isHealthy;
    private LHTaskWorkerHealthReason reason;

    /**
     * All-args constructor for creating a health snapshot.
     *
     * @param isHealthy whether the worker is healthy
     * @param reason the reason for the current health state
     */
    public LHTaskWorkerHealth(boolean isHealthy, LHTaskWorkerHealthReason reason) {
        this.isHealthy = isHealthy;
        this.reason = reason;
    }
}
