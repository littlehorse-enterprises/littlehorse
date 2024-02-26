package io.littlehorse.server.monitoring.metrics;

import io.littlehorse.server.streams.util.LHCache;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;

public class LHCacheMetrics implements MeterBinder {

    public static final String METRIC_NAME = "lh_cache_size";
    public static final String CACHE_NAME_TAG = "cache_name";
    private final LHCache<?, ?> cache;
    private final String name;

    public LHCacheMetrics(final LHCache<?, ?> cache, final String name) {
        this.cache = cache;
        this.name = name;
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        Gauge.builder(METRIC_NAME, cache, LHCache::size)
                .tag(CACHE_NAME_TAG, name)
                .register(registry);
    }
}
