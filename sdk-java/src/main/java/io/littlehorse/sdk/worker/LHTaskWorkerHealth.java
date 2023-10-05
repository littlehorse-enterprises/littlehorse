package io.littlehorse.sdk.worker;

public class LHTaskWorkerHealth {
    private boolean isHealthy;
    private LHTaskWorkerHealthReason reason;

    public LHTaskWorkerHealth(boolean isHealthy, LHTaskWorkerHealthReason reason) {
        this.isHealthy = isHealthy;
        this.reason = reason;
    }

    public boolean isHealthy() {
        return isHealthy;
    }

    public LHTaskWorkerHealthReason getReason() {
        return reason;
    }
}
