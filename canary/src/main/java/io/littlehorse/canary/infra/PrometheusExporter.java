package io.littlehorse.canary.infra;

import io.javalin.http.ContentType;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.prometheusmetrics.PrometheusConfig;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PrometheusExporter implements WebServiceBinder {

    private final PrometheusMeterRegistry prometheusMeterRegistry;
    private final String metricsPath;

    public PrometheusExporter(final String metricsPath, final Map<String, String> commonTags) {
        this.metricsPath = metricsPath;
        prometheusMeterRegistry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
        ShutdownHook.add("Prometheus Exporter", prometheusMeterRegistry::close);

        prometheusMeterRegistry
                .config()
                .commonTags(commonTags.entrySet().stream()
                        .map(entry -> Tag.of(entry.getKey(), entry.getValue()))
                        .toList());
    }

    public void addMeasurable(final MeterBinder measurable) {
        measurable.bindTo(prometheusMeterRegistry);
    }

    @Override
    public void bindTo(final WebServiceRegistry registry) {
        registry.get(metricsPath, ctx -> {
            log.debug("Processing metrics request");
            ctx.contentType(ContentType.PLAIN).result(prometheusMeterRegistry.scrape());
        });
    }
}
