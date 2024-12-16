package io.littlehorse.canary.prometheus;

import io.littlehorse.canary.util.ShutdownHook;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.prometheusmetrics.PrometheusConfig;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import java.util.Map;

public class PrometheusExporter {

    private final PrometheusMeterRegistry prometheusRegistry;

    public PrometheusExporter(final Map<String, String> commonTags) {
        prometheusRegistry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
        ShutdownHook.add("Prometheus Exporter", prometheusRegistry::close);

        prometheusRegistry
                .config()
                .commonTags(commonTags.entrySet().stream()
                        .map(entry -> Tag.of(entry.getKey(), entry.getValue()))
                        .toList());
    }

    public void addMeasurable(final MeterBinder measurable) {
        measurable.bindTo(prometheusRegistry);
    }

    public String scrape() {
        return prometheusRegistry.scrape();
    }
}
