package io.littlehorse.canary.infra;

@FunctionalInterface
public interface HealthStatusSupplier {
    boolean isHealthy();
}
