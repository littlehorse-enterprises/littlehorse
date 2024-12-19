package io.littlehorse.server.monitoring.metrics;

import io.littlehorse.server.streams.util.AsyncWaiters;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;

public class AsyncWaiterMetrics implements MeterBinder {

    private static final String METRIC_NAME = "async_waiter_size";
    private final AsyncWaiters asyncWaiters;

    public AsyncWaiterMetrics(AsyncWaiters asyncWaiters) {
        this.asyncWaiters = asyncWaiters;
    }

    @Override
    public void bindTo(MeterRegistry meterRegistry) {
        Gauge.builder(METRIC_NAME, asyncWaiters, AsyncWaiters::size).register(meterRegistry);
    }
}
