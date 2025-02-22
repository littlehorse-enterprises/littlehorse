package io.littlehorse.canary.prometheus;

import io.javalin.http.ContentType;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.littlehorse.canary.infra.ShutdownHook;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.prometheusmetrics.PrometheusConfig;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PrometheusExporter implements Handler {

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

    @Override
    public void handle(final Context ctx) {
        log.trace("Processing metrics request");
        ctx.contentType(ContentType.PLAIN).result(prometheusRegistry.scrape());
    }
}
