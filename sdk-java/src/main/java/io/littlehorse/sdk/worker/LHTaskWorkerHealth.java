package io.littlehorse.sdk.worker;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Builder
@Getter
@EqualsAndHashCode
public class LHTaskWorkerHealth {
    public static final LHTaskWorkerHealth UNHEALTHY = builder()
            .isHealthy(false)
            .reason(LHTaskWorkerHealthReason.UNHEALTHY)
            .build();
    private boolean isHealthy;
    private LHTaskWorkerHealthReason reason;
}
