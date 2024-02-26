package io.littlehorse.server.monitoring.metrics;

import io.littlehorse.server.streams.util.LHCache;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;

public class LHCacheMetrics implements MeterBinder {

    public static final String METRIC_NAME = "lh_cache_size";
    private final LHCache<?, ?> cache;
    private final String name;

    public LHCacheMetrics(final LHCache<?, ?> cache, final String name) {
        this.cache = cache;
        this.name = name;
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        Gauge.builder(METRIC_NAME, cache, LHCache::size)
                .tag("cache_name", name)
                .register(registry)
                .getId();
    }
}
