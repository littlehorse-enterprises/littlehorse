package io.littlehorse.sdk.worker;

public class LHTaskWorkerHealth {

    //    private final boolean healthy;
    //
    //    private final HealthReason reason;
    //
    //    public LHTaskWorkerHealth(boolean healthy, HealthReason reason){
    //        this.healthy = healthy;
    //        this.reason = reason;
    //    }
    //
    //    public boolean isHealthy() {
    //        return healthy;
    //    }
    //
    //    public HealthReason getReason() {
    //        return reason;
    //    }
    //
    //    enum HealthReason {
    //        HEALTHY, // Can contact lh server and server is healthy
    //        UNHEALTHY, // Is not able to contact lh server or is unhealthy
    //        SERVER_REBALANCING // Can contact lh server and server is unhealthy
    //    }

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
