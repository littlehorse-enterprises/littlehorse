package io.littlehorse.sdk.worker.internal;

import static io.littlehorse.sdk.worker.LHTaskWorkerHealthReason.*;

import io.littlehorse.sdk.common.proto.RegisterTaskWorkerResponse;
import io.littlehorse.sdk.worker.LHTaskWorkerHealth;

public class LHLivenessController {

    private boolean isWorkerHealthy = true;
    private boolean isClusterHealthy = true;
    private boolean isRunning = true;

    public boolean keepWorkerRunning() {
        return isRunning;
    }

    public void stop() {
        isRunning = false;
    }

    public boolean isClusterHealthy() {
        return this.isClusterHealthy;
    }

    public boolean isWorkerHealthy() {
        return isWorkerHealthy;
    }

    public void notifyWorkerFailure() {
        isWorkerHealthy = false;
    }

    public void notifySuccessCall(RegisterTaskWorkerResponse response) {
        if (response.hasIsClusterHealthy()) {
            this.isClusterHealthy = response.getIsClusterHealthy();
        } else {
            this.isClusterHealthy = true;
        }
        isWorkerHealthy = true;
    }

    public LHTaskWorkerHealth healthStatus() {
        if (!isClusterHealthy)
            return LHTaskWorkerHealth.builder()
                    .isHealthy(false)
                    .reason(SERVER_REBALANCING)
                    .build();

        if (!isWorkerHealthy)
            return LHTaskWorkerHealth.builder()
                    .isHealthy(false)
                    .reason(UNHEALTHY)
                    .build();

        return LHTaskWorkerHealth.builder().isHealthy(true).reason(HEALTHY).build();
    }
}
