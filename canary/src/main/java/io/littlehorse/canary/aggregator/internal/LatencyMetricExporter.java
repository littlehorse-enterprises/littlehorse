package io.littlehorse.canary.aggregator.internal;

import io.littlehorse.canary.proto.MetricAverage;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

public class LatencyMetricExporter implements MeterBinder {

    private final ReadOnlyKeyValueStore<String, MetricAverage> store;

    public LatencyMetricExporter(final ReadOnlyKeyValueStore<String, MetricAverage> store) {
        this.store = store;
    }

    @Override
    public void bindTo(final MeterRegistry registry) {
        Gauge.builder("test", () -> (int) (Math.random() * 1000))
                .description("Usable space for path")
                .baseUnit("ms")
                .strongReference(true)
                .register(registry);
    }
}
