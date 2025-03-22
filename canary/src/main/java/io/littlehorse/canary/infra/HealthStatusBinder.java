package io.littlehorse.canary.infra;

public interface HealthStatusBinder {
    void bindTo(final HealthStatusRegistry registry);
}
