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
}
